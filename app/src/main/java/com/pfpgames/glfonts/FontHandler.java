package com.pfpgames.glfonts;

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class FontHandler {
    private SAXParserFactory spf;
    private SAXParser sp;
    private XMLReader xr;

	public void parseFonts(InputStream input, FontLibrary fonts)
	{
        try {
            spf = SAXParserFactory.newInstance();
            sp = spf.newSAXParser();
            xr = sp.getXMLReader();
            xr.setContentHandler(new fontsMain(fonts));
            xr.parse(new InputSource(input));
        } catch (Exception e){
        	e.printStackTrace();
        }
    }

    private class fontsMain extends DefaultHandler{
		private FontLibrary fontsObj = null;
		
    	public fontsMain(FontLibrary fonts) {
			fontsObj = fonts;
		}
    	
		public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
            super.startElement(uri, localName, name, atts);
            
            if (localName.equalsIgnoreCase("font")){
                xr.setContentHandler(new fontsMapping(fontsObj.createMapping(atts.getValue("name"), atts.getValue("file"), atts.getValue("fullfile"))));
            }
        }

        public void endElement(String uri, String localName, String name) throws SAXException {
            super.endElement(uri, localName, name);
        }
        
        private class fontsMapping extends DefaultHandler{
        	private FontMapping fontMap = null;

    		public fontsMapping(FontMapping map) {
				fontMap = map;
			}

			public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
                super.startElement(uri, localName, name, atts);
                
                if (localName.equalsIgnoreCase("character")){
                	fontMap.addCharacter(
	                	Integer.parseInt(atts.getValue("code")),
	                	Float.parseFloat(atts.getValue("centerX")),
	                	Float.parseFloat(atts.getValue("centerY")),
	                	Float.parseFloat(atts.getValue("width")),
	                	Float.parseFloat(atts.getValue("height")));
                }
            }

            public void endElement(String uri, String localName, String name) throws SAXException {
                super.endElement(uri, localName, name);

                if (localName.equalsIgnoreCase("font")){
                	fontMap.finishFont();
                    xr.setContentHandler(new fontsMain(fontsObj));
                }
            }
        }
    }
}
