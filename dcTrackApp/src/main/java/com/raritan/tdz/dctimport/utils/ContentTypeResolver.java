package com.raritan.tdz.dctimport.utils;

import java.io.IOException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.xml.sax.helpers.DefaultHandler;



public class ContentTypeResolver {
	
	public static String getContentType(File file) throws Exception
	{
    	AutoDetectParser parser = new AutoDetectParser();
    	parser.setParsers(new HashMap<MediaType, Parser>());
    	
    	Metadata metadata = new Metadata();
    	metadata.add(TikaMetadataKeys.RESOURCE_NAME_KEY, file.getName());
    	
    	InputStream stream = new FileInputStream(file);
    	parser.parse(stream, new DefaultHandler(), metadata,new ParseContext());
    	stream.close();
    	
    	String mimeType = metadata.get(HttpHeaders.CONTENT_TYPE);
    	
    	return mimeType;
	}
}
