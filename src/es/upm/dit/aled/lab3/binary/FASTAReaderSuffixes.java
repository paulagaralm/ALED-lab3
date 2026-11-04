package es.upm.dit.aled.lab3.binary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.upm.dit.aled.lab3.FASTAReader;

/**
 * Reads a FASTA file containing genetic information and allows for the search
 * of specific patterns within these data. The information is stored as an array
 * of bytes that contain nucleotides in the FASTA format. Since this array is
 * usually created before knowing how many characters in the origin FASTA file
 * are valid, an int indicating how many bytes of the array are valid is also
 * stored. All valid characters will be at the beginning of the array.
 * 
 * This extension of the FASTAReader uses a sorted dictionary of suffixes to
 * allow for the implementation of binary search.
 * 
 * @author mmiguel, rgarciacarmona
 *
 */
public class FASTAReaderSuffixes extends FASTAReader {
	protected Suffix[] suffixes;

	/**
	 * Creates a new FASTAReader from a FASTA file.
	 * 
	 * At the end of the constructor, the data is sorted through an array of
	 * suffixes.
	 * 
	 * @param fileName The name of the FASTA file.
	 */
	public FASTAReaderSuffixes(String fileName) {
		// Calls the parent constructor
		super(fileName);
		this.suffixes = new Suffix[validBytes];
		for (int i = 0; i < validBytes; i++)
			suffixes[i] = new Suffix(i);
		// Sorts the data
		sort();
	}

	/*
	 * Helper method that creates a array of integers that contains the positions of
	 * all suffixes, sorted alphabetically by the suffix.
	 */
	private void sort() {
		// Instantiate the external SuffixComparator, passing 'this' (the reader)
		// so it can access the content and validBytes fields.
		SuffixComparator suffixComparator = new SuffixComparator(this);
		// Use the external Comparator for sorting.
		Arrays.sort(this.suffixes, suffixComparator);
	}

	/**
	 * Prints a list of all the suffixes and their position in the data array.
	 */
	public void printSuffixes() {
		System.out.println("-------------------------------------------------------------------------");
		System.out.println("Index | Sequence");
		System.out.println("-------------------------------------------------------------------------");
		for (int i = 0; i < suffixes.length; i++) {
			int index = suffixes[i].suffixIndex;
			String ith = "\"" + new String(content, index, Math.min(50, validBytes - index)) + "\"";
			System.out.printf("  %3d | %s\n", index, ith);
		}
		System.out.println("-------------------------------------------------------------------------");
	}

	/**
	 * Implements a binary search to look for the provided pattern in the data
	 * array. Returns a List of Integers that point to the initial positions of all
	 * the occurrences of the pattern in the data.
	 * 
	 * @param pattern The pattern to be found.
	 * @return All the positions of the first character of every occurrence of the
	 *         pattern in the data.
	 */
	@Override
	public List<Integer> search(byte[] pattern) {
		//O(m*log(n)) siendo n el tamaño de suffixes y m el de pattern para un solo elemento y O(n*m) para todos
		
		//Inicialización: límites de búsqueda, empezando por la primera y última posición de suffixes
		List<Integer> posiciones = new ArrayList<>(); //lista a devolver
		int lo = 0;
		int hi = this.suffixes.length - 1;
		
		//Búsqueda binaria
		while(lo <= hi) { //mientras el intervalo tenga algún elemento
			int m = lo + ((hi - lo) / 2); //se calcula el índice medio
			int posSuffix = suffixes[m].suffixIndex; //se busca el sufijo correspondiente al índice medio
			boolean found = true; //variable para saber si se ha encontrado o no alguna coincidencia
			int i; //variable para el for y para conservar su valor
			
			for(i = 0; i < pattern.length-1; i++) { //se recorre pattern para compararlo completo
				if(pattern[i] != content[posSuffix + i]) { //si encuentra caracteres diferentes entre pattern y content
					found = false; //no se ha encontrado una coincidencia
					break; //sale del for
				}
			}
			
			if(found) { //si se ha encontrado una coincidencia (for completo)
				posiciones.add(posSuffix); //se añade su posición a la lista

// CÓDIGO PARA UNIFICAR IZQUIERDA Y DERECHA
//				int despl = 1;
//				found = true;
//				boolean foundizq = true;
//				boolean founddcha = true;
//				while (found) {
//					if((m - despl) >= 0) {
//						int posSuffixizq = suffixes[m - despl].suffixIndex;
//					}
//					else {
//						foundizq = false;
//					}
//					if((m + despl) <= suffixes.length-1) {
//						int posSuffixdcha = suffixes[m + despl].suffixIndex;
//					}
//					else {
//						founddcha = false;
//					}
//					for(i = 0; i < pattern.length-1; i++) {
//						if(foundizq && (pattern[i] != content[posSuffixizq + i])) {
//							foundizq = false;
//						}
//						if(founddcha && (pattern[i] != content[posSuffixdcha + i])) {
//							founddcha = false;
//						}
//						if (!foundizq && !founddcha) {
//							found = false;
//							break;
//						}
//					}
//					if(foundizq) {
//						posiciones.add(posSuffixizq);
//					}
//					else if(founddcha) {
//						posiciones.add(posSuffixdcha);						
//					}
//					else {
//						break;
//					}
//					despl++;
//				}
				
				//CÓDIGO PARA BUSCAR LAS DEMÁS POSICIONES SABIENDO QUE ESTÁN AL LADO AL ESTAR ORDENADAS
				int izq = m - 1; //se calcula el primer índice a la izquierda
				found = true; //se ha encontrado una coincidencia
				while(izq >= 0) { //mientras que el índice sea mayor o igual a 0
					posSuffix = suffixes[izq].suffixIndex; //se busca el sufijo correspondiente al índice izquierdo
					for(i = 0; i < pattern.length-1; i++) { //se recorre pattern
						if(pattern[i] != content[posSuffix + i]) { //si encuentra caracteres diferentes entre pattern y content
							found = false; //no se ha encontrado una coincidencia por la izquierda
							break; //sale del for
						}
					}
					if(found) { //si se ha encontrado una coincidencia por la izquierda (for completo)
						posiciones.add(posSuffix); //se añade su posición a la lista
						izq--; //se desplaza otro valor más a la izquierda
					}
					else { //si no hay coincidencia
						break; //sale del while
					}
				}

				int dcha = m + 1; //se calcula el primer índice a la derecha
				found = true; //se ha encontrado una coincidencia
				while(dcha <= this.suffixes.length-1) { //mientras que el índice sea menor o igual a la longitud de suffixes
					posSuffix = suffixes[dcha].suffixIndex; //se busca el sufijo correspondiente al índice derecho
					for(i = 0; i < pattern.length-1; i++) { //se recorre pattern
						if(pattern[i] != content[posSuffix + i]) { //si encuentra caracteres diferentes entre pattern y content
							found = false; //no se ha encontrado una coincidencia por la izquierda
							break; //sale del for
						}
					}
					if(found) { //si se ha encontrado una coincidencia por la derecha (for completo)
						posiciones.add(posSuffix); //se añade su posición a la lista
						dcha++; //se desplaza otro valor más a la derecha
					}
					else { //si no hay coincidencia
						break; //sale del while
					}
				}

				break; //termina la búsqueda tras haber encontrado todas las coincidencias
			}
			
			//Continua la búsqueda al no haber encontrado coincidencia en la mitad de ese intervalo
			if(pattern[i] < content[posSuffix + i]) { //si el carácter de pattern es menor que el de content
				hi = m - 1; //se descarta la mitad derecha de suffixes
			}
			
			else if(pattern[i] > content[posSuffix + i]) { //si el carácter de pattern es mayor que el de content
				lo = m + 1; //se descarta la mitad izquierda de suffixes
			}
		}
		return posiciones; //se devuelve la lista con las coincidencias
	}

	public static void main(String[] args) {
		long t1 = System.nanoTime();
		FASTAReaderSuffixes reader = new FASTAReaderSuffixes(args[0]);
		if (args.length == 1)
			return;
		byte[] patron = args[1].getBytes();
		System.out.println("Tiempo de apertura de fichero: " + (System.nanoTime() - t1));
		long t2 = System.nanoTime();
		System.out.println("Tiempo de ordenación: " + (System.nanoTime() - t2));
		reader.printSuffixes();
		long t3 = System.nanoTime();
		List<Integer> posiciones = reader.search(patron);
		System.out.println("Tiempo de búsqueda: " + (System.nanoTime() - t3));
		if (posiciones.size() > 0) {
			for (Integer pos : posiciones)
				System.out.println("Encontrado " + args[1] + " en " + pos);
		} else
			System.out.println("No he encontrado " + args[1] + " en ningún sitio.");
		System.out.println("Tiempo total: " + (System.nanoTime() - t1));
	}
}
