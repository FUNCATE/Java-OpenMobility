/*
 * Copyright 2014, Augmented Technologies Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.augtech.geoapi.context;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.opengis.context.Content;
import org.opengis.context.Offering;
import org.opengis.context.Operation;

import com.augtech.geoapi.utils.DataUtilities;
import com.augtech.geoapi.utils.WebRequest;

/** A few utilities to aid processing Context Document information
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class Utils {
	static final int BUFFER = 1024;
	
	/** Get a comma separated String of feature type names the offering is restricted
	 * to.
	 * 
	 * @param offering
	 * @return
	 */
	public static String getTypesFromOffering(Offering offering) {
		String oCode = "";
		
		for (Operation o : offering.getOperations()) {
			oCode = o.getCode();
			if (oCode.equals("GetFeature") ) {
				// WFS
				return getLayerFromURL(o.getURI(), "typename");
			} else if (oCode.equals("GetMap") ) {
				// WMS
				return getLayerFromURL(o.getURI(), "layers");

			} else if (oCode.equals("GetTables")) {
				// GeoPackage
				return o.getURI().toString().trim();
				
			} else if (oCode.equals("GetTile")) {
				// WMTS
				return getLayerFromURL(o.getURI(), "layer");
			}
		}
		
		return "*";
	}
	private static String getLayerFromURL(URI uri, String paramName) {
		for (String s : uri.getQuery().toLowerCase().split("&")) {
			if (s.startsWith(paramName)) return s.split("=")[1];
		}
		return "*";
	}
	/** Copy all in-line contents or results from the supplied {@link Offering} to
	 * a single file.
	 * 
	 * @param offering The Offering to process
	 * @param outFile The resultant file
	 * @throws Exception
	 */
	public static void contextContentsToFile(Offering offering, File outFile) 
			throws Exception {
		
		if (offering.getContentsCount()>0) {

			for (Content c : offering.getContents()) {

				if (c.getURI()!=null) {
					// Referenced content
					throw new IllegalArgumentException("Referenced Context Content not implemented yet!");
				} else {
					// In-line content
					streamCopy(
							new ByteArrayInputStream(c.getContent().getBytes()), 
							new FileOutputStream(outFile, true)
							);
				}
			}
		} else {
			// No contents, so must be 'result'
			for (Operation o : offering.getOperations()) {
				if (o.getResult().getURI()!=null) {
					// referenced result
					throw new IllegalArgumentException("Referenced Context Result not implemented yet!");
				} else {
					// in-line result
					o.getResult().getContent();

					streamCopy(
							new ByteArrayInputStream(o.getResult().getContent().getBytes()), 
							new FileOutputStream(outFile, true)
							);

				}

			}
		}
	}
	/** Download online or process in-line context content from the supplied Offering source
	 * into a single file stored locally. If the contents is in a zip file then all files in
	 * the zip are extracted and the first file with a matching extension is returned.<p>
	 * 
	 * If the target file exists nothing is done and that file name is returned, therefore the 
	 * supplied directory should be unique to the offering and external referesh/ removal
	 * processes put in place. 
	 * 
	 * @param offeringToUse The {@link Offering} to process.
	 * @param directory The local directory to create/ un-zip content.
	 * @return The file where the content is stored. The file extension is taken from the last 
	 * part of the offering getCode() method.
	 *   
	 * @throws Exception 
	 */
	public static File getContextContent(Offering offeringToUse, File directory) throws Exception {
		/* Am not expecting more than one local file within a single
		 * offering */
		directory.mkdirs();
		
		String ext = offeringToUse.getCode().toString();
		ext = ext.substring(ext.lastIndexOf("/")+1);
		File localFile = null;
		URI uri = offeringToUse.getContents().get(0).getURI();

		if (uri==null) {

			localFile = new File(directory, offeringToUse.getID()+"."+ext );
			// This has to be in-line content, so make a single file to read from
			if (localFile.exists()) return localFile;
			
			contextContentsToFile(offeringToUse, localFile);

		} else {

			// Build a file name to download to under a Dataset specific directory
			localFile = new File(directory, new File(uri.getPath()).getName() );

			if (localFile.exists()) {
				
				localFile = getFirstFile(directory, ext);
				
			} else {

				// Pull down the content to a local file
				WebRequest wr = new WebRequest( uri.toString() );
				InputStream is = wr.openConnection();
				streamCopy(is, new FileOutputStream(localFile) );

				/* Could be a zip file containing multiple files (i.e shape), so decompress
				 * and get the first file matching our extension */
				DataUtilities.unZipArchive( localFile.toString(), directory.toString() );
				localFile = getFirstFile(directory, ext);

			}
		}
		
		return localFile;
	}
	/** Get the first file with a matching file extension within a directory.
	 * 
	 * @param directory
	 * @param ext
	 * @return
	 */
	static File getFirstFile(File directory, String ext) {
		for (File f : directory.listFiles()) {
			if (f.toString().endsWith(ext)) return f;
		}
		return null;
	}
	/** Copy an inputstream to an output stream. Generally used for saving 
	 * to disk from download. Both streams are closed on completion.
	 * 
	 * @param in InputStream  Source
	 * @param out OutputStream  Destination
	 * @throws IOException
	 */
    public static void streamCopy(InputStream in, OutputStream out) throws IOException{
        byte[] b = new byte[BUFFER];
        int read;
        while ((read = in.read(b)) != -1) {
                out.write(b, 0, read);
        }
        out.close();
        in.close();
    }
	
}