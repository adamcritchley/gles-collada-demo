package com.acritchley.collada;

import java.io.InputStream;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class ColladaHandler {
    private SAXParserFactory spf;
    private SAXParser sp;
    private XMLReader xr;

    public void parseDae(InputStream input, ColladaModel model) {
    	ColladaObjects obj = new ColladaObjects();

        try {
            spf = SAXParserFactory.newInstance();
            sp = spf.newSAXParser();
            xr = sp.getXMLReader();
            xr.setContentHandler(new colladaMain(obj));
            xr.parse(new InputSource(input));
        } catch (Exception e){
        	e.printStackTrace();
        }

        // Build the model from the COLLADA file...
        obj.buildModel(model);
    }

    private String objRefToObj(String ref){
    	// Remove the # character from references...
    	return ref.substring(1);
    }

    private class colladaMain extends DefaultHandler{
    	private ColladaObjects daeObj = null;
   
        public colladaMain(ColladaObjects obj) {
        	daeObj = obj;
		}

		public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
            super.startElement(uri, localName, name, atts);
            
            if (localName.equalsIgnoreCase("library_geometries")){
                xr.setContentHandler(new colladaLibraryGeometries(daeObj));
            }else if (localName.equalsIgnoreCase("library_images")){
                xr.setContentHandler(new colladaLibraryImages(daeObj));
            }else if (localName.equalsIgnoreCase("library_effects")){
                xr.setContentHandler(new colladaLibraryEffects(daeObj));
            }else if (localName.equalsIgnoreCase("library_materials")){
                xr.setContentHandler(new colladaLibraryMaterials(daeObj));
            }else if (localName.equalsIgnoreCase("library_visual_scenes")){
                xr.setContentHandler(new colladaLibraryScenes(daeObj));
            }else if (localName.equalsIgnoreCase("library_controllers")){
                xr.setContentHandler(new colladaLibraryControllers(daeObj));
            }else if (localName.equalsIgnoreCase("scene")){
                xr.setContentHandler(new colladaScene(daeObj));
            }else if(localName.equalsIgnoreCase("asset")){
                xr.setContentHandler(new colladaAsset());
            }
        }

        public void endElement(String uri, String localName, String name) throws SAXException {
            super.endElement(uri, localName, name);
        }
        
        private class colladaAsset extends DefaultHandler{
        	private boolean inCreated = false;
        	private boolean inModified = false;
        	private boolean inContributor = false;
			private boolean inAuthor = false;
			private boolean inAuthoringTool = false;
        	
        	public colladaAsset(){
        	}

            public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
                super.startElement(uri, localName, name, atts);
                
                if (localName.equalsIgnoreCase("created")){
                	inCreated = true;
                }else if (localName.equalsIgnoreCase("modified")){
                	inModified = true;
                }else if (localName.equalsIgnoreCase("contributor")){
                	inContributor = true;
                }else if (localName.equalsIgnoreCase("unit")){
                	daeObj.setUnits(atts.getValue("name"));
                	daeObj.setMeter(atts.getValue("meter"));
                }else if (localName.equalsIgnoreCase("author")){
                	inAuthor = true;
                }else if (localName.equalsIgnoreCase("authoring_tool")){
                	inAuthoringTool = true;
                }
            }

            public void endElement(String uri, String localName, String name) throws SAXException {
                super.endElement(uri, localName, name);
                
                if (localName.equalsIgnoreCase("asset")){
                	xr.setContentHandler(new colladaMain(daeObj));
                }else if (localName.equalsIgnoreCase("modified")){
                	inModified = false;
                }else if(localName.equalsIgnoreCase("created")){
                	inCreated = false;
                }else if (localName.equalsIgnoreCase("contributor")){
                	inContributor = false;
                }else if (localName.equalsIgnoreCase("author")){
                	inAuthor = false;
                }else if (localName.equalsIgnoreCase("authoring_tool")){
                	inAuthoringTool = false;
                }
            }

            public void characters(char[] ch, int start, int length) throws SAXException {
                super.characters(ch, start, length);
                String text = new String(ch, start, length);

                if( inCreated ){
                	daeObj.setCreated(text);
                }else if( inModified ){
                	daeObj.setModified(text);
                }else if( inContributor ){
                	if( inAuthor ){
                		daeObj.setAuthor(text);
                	}else if( inAuthoringTool ){
                		daeObj.setAuthoringTool(text);
                	}
                }
            }
        }
        
        private class colladaLibraryControllers extends DefaultHandler{
        	private ColladaObjects daeObj = null;

        	public colladaLibraryControllers(ColladaObjects obj){
        		daeObj = obj;
        	}

            public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
                super.startElement(uri, localName, name, atts);
                
                if (localName.equalsIgnoreCase("controller")){
                	xr.setContentHandler(new colladaController(daeObj.createController(atts.getValue("id"))));
                }
            }

            public void endElement(String uri, String localName, String name) throws SAXException {
                super.endElement(uri, localName, name);
                
                if (localName.equalsIgnoreCase("library_controllers")){
                	xr.setContentHandler(new colladaMain(daeObj));
                }
            }
            
            private class colladaController extends DefaultHandler{
            	private ColladaController daeCtrl = null;

            	public colladaController(ColladaController ctrl){
            		daeCtrl = ctrl;
            	}

                public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
                    super.startElement(uri, localName, name, atts);
                    
                    if (localName.equalsIgnoreCase("skin")){
                    	xr.setContentHandler(new colladaSkin(daeCtrl.createSkin(daeObj.getGeometryById(objRefToObj(atts.getValue("source"))))));
                    }
                }

                public void endElement(String uri, String localName, String name) throws SAXException {
                    super.endElement(uri, localName, name);
                    
                    if (localName.equalsIgnoreCase("controller")){
                    	xr.setContentHandler(new colladaLibraryControllers(daeObj));
                    }
                }
                
                private class colladaSkin extends DefaultHandler{
                	private boolean inBindShapeMatrix = false;
                	private String bsmText = "";
                	private ColladaSkin daeSkin = null;

                	public colladaSkin(ColladaSkin skin){
                		daeSkin = skin;
                	}

                    public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
                        super.startElement(uri, localName, name, atts);
                        
                        if (localName.equalsIgnoreCase("bind_shape_matrix")){
                        	inBindShapeMatrix = true;
                        }else if (localName.equalsIgnoreCase("source")){
                        	xr.setContentHandler(new colladaSource(daeSkin.createSource(atts.getValue("id"))));
                        }else if (localName.equalsIgnoreCase("joints")){
                        	xr.setContentHandler(new colladaJoints(daeSkin.createJoints()));
                        }else if (localName.equalsIgnoreCase("vertex_weights")){
                        	xr.setContentHandler(new colladaVertexWeights(daeSkin.createVertexWeights()));
                        }
                    }

                    public void endElement(String uri, String localName, String name) throws SAXException {
                        super.endElement(uri, localName, name);
                        
                        if (localName.equalsIgnoreCase("skin")){
                        	xr.setContentHandler(new colladaController(daeCtrl));
                        }else if (localName.equalsIgnoreCase("bind_shape_matrix")){
                        	inBindShapeMatrix = false;

		                	Pattern p = Pattern.compile("\\s+");
		                	String[] temp = p.split(bsmText.trim());
				        	float[] matrixData = new float[temp.length];

							for (int i=0; i < temp.length; i++){
								matrixData[i] = Float.parseFloat(temp[i]);
							}

							daeSkin.setBindShapeMatrix(new ColladaMatrix4f(
									matrixData[0], matrixData[4], matrixData[8], matrixData[12],
									matrixData[1], matrixData[5], matrixData[9], matrixData[13],
									matrixData[2], matrixData[6], matrixData[10], matrixData[14],
									matrixData[3], matrixData[7], matrixData[11], matrixData[15]));
                        }
                    }
                    
                    public void characters(char[] ch, int start, int length) throws SAXException {
                        super.characters(ch, start, length);
                        String text = new String(ch, start, length);

                        if( inBindShapeMatrix ){
                        	bsmText += text;
                        }
                    }
                    
    		        private class colladaSource extends DefaultHandler{
    		            private boolean inFloatArray = false;
						private boolean inNameArray = false;
    		        	private ColladaSource srcObj = null;
    		        	private int floatSize = 0;
    		        	private String floatText = "";
						private String nameText = "";
						private int nameSize;
    		            
    		            public colladaSource(ColladaSource obj) {
    		            	srcObj = obj;
    		    		}

    		    		public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
    		                super.startElement(uri, localName, name, atts);
    		                
    		                if (localName.equalsIgnoreCase("float_array")){
    		                    inFloatArray = true;
    		                    floatSize = Integer.parseInt(atts.getValue("count"));
    		                }else if(localName.equalsIgnoreCase("Name_array")){
    		                	inNameArray = true;
    		                    nameSize = Integer.parseInt(atts.getValue("count"));
    		                }
    		            }

    		            public void endElement(String uri, String localName, String name) throws SAXException {
    		                super.endElement(uri, localName, name);
    		                
    		                if (localName.equalsIgnoreCase("float_array")){
    		                    inFloatArray = false;
    				        	float[] arrayData = new float[floatSize];
    		                	Pattern p = Pattern.compile("\\s+");
    		                	String[] temp = p.split(floatText.trim());
    		                    if( (floatSize != temp.length) && (floatSize > 0) ){
    		                    	throw new SAXException("Inconsistent source size for " + srcObj.getId());
    		                    }

    							for (int i=0; i < temp.length; i++){
    								arrayData[i] = Float.parseFloat(temp[i]);
    							}

    							ColladaSource.FloatArray floatArray = srcObj.getFloatArray();
    							floatArray.setFloatData(arrayData);
    		                }else if(localName.equalsIgnoreCase("Name_array")){
    		                	inNameArray = false;
    		                	Pattern p = Pattern.compile("\\s+");
    		                	String[] temp = p.split(nameText.trim());
    		                    if( (nameSize != temp.length) && (nameSize > 0) ){
    		                    	throw new SAXException("Inconsistent source size for " + srcObj.getId());
    		                    }
    		                	
    		                	ColladaSource.NameArray nameArray = srcObj.getNameArray();
    		                	nameArray.setNameData(temp);
    		                }else if (localName.equalsIgnoreCase("source")){
    		                	xr.setContentHandler(new colladaSkin(daeSkin));                	
    		                }
    		            }

    		            public void characters(char[] ch, int start, int length) throws SAXException {
    		                super.characters(ch, start, length);
    		                String text = new String(ch, start, length);

    		                if (inFloatArray){
    		                	floatText += text;
    		                }else if(inNameArray){
    		                	nameText += text;
    		                }
    		            }
    		        }
    		        
    	            private class colladaJoints extends DefaultHandler{
    	            	private ColladaJoints daeJoints = null;

    	            	public colladaJoints(ColladaJoints obj){
    	            		daeJoints = obj;
    	            	}

    	                public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
    	                    super.startElement(uri, localName, name, atts);
    	                    
    	                    if (localName.equalsIgnoreCase("input")){
    	                    	if( atts.getValue("semantic").equalsIgnoreCase("JOINT") ){
    	                    		daeJoints.useJoints(
    		                				daeSkin.getSourceById(objRefToObj(atts.getValue("source"))));
    		                	}else if( atts.getValue("semantic").equalsIgnoreCase("INV_BIND_MATRIX") ){
    		                		daeJoints.useInverseBindMatrix(
    		                				daeSkin.getSourceById(objRefToObj(atts.getValue("source"))));
    		                	}
    	                    }
    	                }

    	                public void endElement(String uri, String localName, String name) throws SAXException {
    	                    super.endElement(uri, localName, name);
    	                    
    	                    if (localName.equalsIgnoreCase("joints")){
    	                    	xr.setContentHandler(new colladaSkin(daeSkin));
    	                    }
    	                }
    	            }
    	            
    	            private class colladaVertexWeights extends DefaultHandler{
    	            	private boolean inVCount = false;
    	            	private boolean inV = false;
    	            	private ColladaVertexWeights daeVrtxWeights = null;
						private String vText = "";
						private String vcntText = "";

    	            	public colladaVertexWeights(ColladaVertexWeights weights){
    	            		daeVrtxWeights = weights;
    	            	}

    	                public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
    	                    super.startElement(uri, localName, name, atts);
    	                    
    	                    if (localName.equalsIgnoreCase("input")){
    	                    	if( atts.getValue("semantic").equalsIgnoreCase("JOINT") ){
    	                    		daeVrtxWeights.useJoints(
    		                				daeSkin.getSourceById(objRefToObj(atts.getValue("source"))),
    		                				Integer.parseInt(atts.getValue("offset")));
    		                	}else if( atts.getValue("semantic").equalsIgnoreCase("WEIGHT") ){
    		                		daeVrtxWeights.useWeight(
    		                				daeSkin.getSourceById(objRefToObj(atts.getValue("source"))),
    		                				Integer.parseInt(atts.getValue("offset")));
    		                	}
    	                    }else if (localName.equalsIgnoreCase("vcount")){
    	                    	inVCount = true;
    	                    }else if (localName.equalsIgnoreCase("v")){
    	                    	inV = true;
    	                    }
    	                }

    	                public void endElement(String uri, String localName, String name) throws SAXException {
    	                    super.endElement(uri, localName, name);
    	                    
    	                    if (localName.equalsIgnoreCase("vertex_weights")){
    	                    	xr.setContentHandler(new colladaSkin(daeSkin));
    	                    }else if (localName.equalsIgnoreCase("vcount")){
    	                    	inVCount = false;
    		                	Pattern p = Pattern.compile("\\s+");
    		                	String[] temp = p.split(vcntText.trim());
    				        	int[] arrayData = new int[temp.length];

    							for (int i=0; i < temp.length; i++){
    								arrayData[i] = Integer.parseInt(temp[i]);
    							}

    							daeVrtxWeights.setVertexCount(arrayData);
    	                    }else if (localName.equalsIgnoreCase("v")){
    	                    	inV = false;
    		                	Pattern p = Pattern.compile("\\s+");
    		                	String[] temp = p.split(vText.trim());
    				        	int[] arrayData = new int[temp.length];

    							for (int i=0; i < temp.length; i++){
    								arrayData[i] = Integer.parseInt(temp[i]);
    							}
    							
    							daeVrtxWeights.setIndices(arrayData);
    	                    }
    	                }
    	                
    		            public void characters(char[] ch, int start, int length) throws SAXException {
    		                super.characters(ch, start, length);
    		                String text = new String(ch, start, length);

    		                if (inV){
    		                	vText += text;
    		                }else if(inVCount){
    		                	vcntText += text;
    		                }
    		            }
    	            }
                }
            }
        }

        private class colladaLibraryImages extends DefaultHandler{
        	private ColladaObjects daeObj = null;
     	   
            public colladaLibraryImages(ColladaObjects obj) {
            	daeObj = obj;
    		}

    		public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
                super.startElement(uri, localName, name, atts);
                
                if (localName.equalsIgnoreCase("image")){
                    xr.setContentHandler(
                    		new colladaImage(
                    				daeObj.createImagery(atts.getValue("id"),atts.getValue("name"))));
                }
            }

            public void endElement(String uri, String localName, String name) throws SAXException {
                super.endElement(uri, localName, name);
                
                if (localName.equalsIgnoreCase("library_images")){
                    xr.setContentHandler(new colladaMain(daeObj));
                }
            }
            
            private class colladaImage extends DefaultHandler{
            	private ColladaImage daeImg;
            	private boolean inIF = false;

				public colladaImage(ColladaImage img) {
					daeImg = img;
				}
            	
	    		public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
	                super.startElement(uri, localName, name, atts);
	                
	                if (localName.equalsIgnoreCase("init_from")){
	                	inIF = true;
	                }
	            }

	            public void endElement(String uri, String localName, String name) throws SAXException {
	                super.endElement(uri, localName, name);
	                
	                if (localName.equalsIgnoreCase("image")){
	                    xr.setContentHandler(new colladaLibraryImages(daeObj));
	                }else if (localName.equalsIgnoreCase("init_from")){
	                	inIF = false;
	                }
	            }
	            
	            public void characters(char[] ch, int start, int length) throws SAXException {
	                super.characters(ch, start, length);
	                String text = new String(ch, start, length);
	                
	                if (inIF){
	                	daeImg.setSource(text);
	                }
	            }
            }
        }
        
        private class colladaLibraryEffects extends DefaultHandler{
        	private ColladaObjects daeObj = null;
     	   
            public colladaLibraryEffects(ColladaObjects obj) {
            	daeObj = obj;
    		}

    		public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
                super.startElement(uri, localName, name, atts);
                
                if (localName.equalsIgnoreCase("effect")){
                	xr.setContentHandler(new colladaEffect(daeObj.createEffect(atts.getValue("id"))));
                }
            }

            public void endElement(String uri, String localName, String name) throws SAXException {
                super.endElement(uri, localName, name);
                
                if (localName.equalsIgnoreCase("library_effects")){
                    xr.setContentHandler(new colladaMain(daeObj));
                }
            }
            
            private class colladaEffect extends DefaultHandler{
            	ColladaEffect daeEffect;

				public colladaEffect(ColladaEffect effect) {
					daeEffect = effect;
				}
            	
	    		public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
	                super.startElement(uri, localName, name, atts);
	                
	                if (localName.equalsIgnoreCase("profile_COMMON")){
	                	xr.setContentHandler(new colladaEffectCommon(daeEffect.createColladaProfile()));
	                }else if (localName.equalsIgnoreCase("profile_GLES")){
	                	//xr.setContentHandler(new colladaEffectGLES());
	                }else if (localName.equalsIgnoreCase("profile_GLSL")){
	                	//xr.setContentHandler(new colladaEffectGLSL());
	                }else if (localName.equalsIgnoreCase("profile_CG")){
	                	// not supported
	                }
	            }

	            public void endElement(String uri, String localName, String name) throws SAXException {
	                super.endElement(uri, localName, name);
	                
	                if (localName.equalsIgnoreCase("effect")){
	                    xr.setContentHandler(new colladaLibraryEffects(daeObj));
	                }
	            }
	            
	            private class colladaEffectCommon extends DefaultHandler{
	            	private ColladaProfileCommon daeProfile;

					public colladaEffectCommon(ColladaProfileCommon profile) {
						daeProfile = profile;
					}
	            	
		    		public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
		                super.startElement(uri, localName, name, atts);
		                
		                if (localName.equalsIgnoreCase("newparam")){
		                	xr.setContentHandler(new colladaEffectParam(daeProfile.createParam(atts.getValue("sid"))));
		                }else if (localName.equalsIgnoreCase("technique")){
		                	xr.setContentHandler(new colladaEffectTechnique(daeProfile.createTechnique(atts.getValue("sid"))));
		                }else if (localName.equalsIgnoreCase("extra")){
		                	xr.setContentHandler(new colladaEffectExtra());
		                }
		            }

		            public void endElement(String uri, String localName, String name) throws SAXException {
		                super.endElement(uri, localName, name);
		                
		                if (localName.equalsIgnoreCase("profile_COMMON")){
		                    xr.setContentHandler(new colladaEffect(daeEffect));
		                }
		            }
		            
		            private class colladaEffectExtra extends DefaultHandler{
			            public void endElement(String uri, String localName, String name) throws SAXException {
			                super.endElement(uri, localName, name);
			                
			                if (localName.equalsIgnoreCase("extra")){
			                    xr.setContentHandler(new colladaEffectCommon(daeProfile));
			                }
			            }
		            }
		           
		            private class colladaEffectParam extends DefaultHandler{
		            	private ColladaParam daeParam;

						public colladaEffectParam(ColladaParam param) {
							daeParam = param;
						}
						
			    		public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
			                super.startElement(uri, localName, name, atts);

			                if (localName.equalsIgnoreCase("surface")){
			                	if(atts.getValue("type") == null){
			                		xr.setContentHandler(new colladaEffectParamSurface((ColladaFxSurface) daeParam.createSurface2D()));
			                	}else if(atts.getValue("type").equalsIgnoreCase("1D")){
			                		// TODO: implement 1D surface
			                	}else if(atts.getValue("type").equalsIgnoreCase("2D")){
			                		xr.setContentHandler(new colladaEffectParamSurface((ColladaFxSurface) daeParam.createSurface2D()));			                		
			                	}else if(atts.getValue("type").equalsIgnoreCase("3D")){
			                		// TODO: implement 3D surface
			                	}
			                }else if (localName.equalsIgnoreCase("sampler2D")){
			                	xr.setContentHandler(new colladaEffectParamSampler2D((ColladaFxSampler) daeParam.createSampler2D()));         	
			                }
			            }

			            public void endElement(String uri, String localName, String name) throws SAXException {
			                super.endElement(uri, localName, name);
			                
			                if (localName.equalsIgnoreCase("newparam")){
			                    xr.setContentHandler(new colladaEffectCommon(daeProfile));
			                }
			            }
			            

			            private class colladaEffectParamSurface extends DefaultHandler{
			            	private ColladaFxSurface daeFxSurface;
			            	private boolean inInit_From = false;

							public colladaEffectParamSurface(ColladaFxSurface surface) {
								daeFxSurface = surface;
							}
							
				    		public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
				                super.startElement(uri, localName, name, atts);
				                
				                if (localName.equalsIgnoreCase("format")){
				                	// TODO: format				                	
				                }else if (localName.equalsIgnoreCase("format_hint")){
				                	// TODO: format_hint				                	
				                }else if (localName.equalsIgnoreCase("size")){
				                	// TODO: size				                	
				                }else if (localName.equalsIgnoreCase("viewport_ratio")){
				                	// TODO: viewport_ratio				                	
				                }else if (localName.equalsIgnoreCase("mip_levels")){
				                	// TODO: mip_levels				                	
				                }else if (localName.equalsIgnoreCase("mipmap_generate")){
				                	// TODO: mipmap_generate				                	
				                }else if (localName.equalsIgnoreCase("init_from")){
				                	inInit_From = true;
				                }else if (localName.equalsIgnoreCase("generator")){
				                	// TODO: generator				                	
				                }
				            }

				            public void endElement(String uri, String localName, String name) throws SAXException {
				                super.endElement(uri, localName, name);
				                
				                if (localName.equalsIgnoreCase("format")){
				                	// TODO: format
				                }else if (localName.equalsIgnoreCase("format_hint")){
				                	// TODO: format_hint				                	
				                }else if (localName.equalsIgnoreCase("size")){
				                	// TODO: size				                	
				                }else if (localName.equalsIgnoreCase("viewport_ratio")){
				                	// TODO: viewport_ratio				                	
				                }else if (localName.equalsIgnoreCase("mip_levels")){
				                	// TODO: mip_levels				                	
				                }else if (localName.equalsIgnoreCase("mipmap_generate")){
				                	// TODO: mipmap_generate				                	
				                }else if (localName.equalsIgnoreCase("init_from")){
				                	inInit_From = false;
				                }else if (localName.equalsIgnoreCase("generator")){
				                	// TODO: generator				                	
				                }else if (localName.equalsIgnoreCase("surface")){
				                    xr.setContentHandler(new colladaEffectParam(daeParam));
				                }
				            }
				            
				            public void characters(char[] ch, int start, int length) throws SAXException {
				                super.characters(ch, start, length);
				                String text = new String(ch, start, length);

				                if( inInit_From ){
				                	daeFxSurface.useImage(daeObj.getImageryById(text));
				                }
				            }
			            }

			            private class colladaEffectParamSampler2D extends DefaultHandler{
			            	private ColladaFxSampler daeFxSampler;
							private boolean inSource = false;

							public colladaEffectParamSampler2D(ColladaFxSampler sampler) {
								daeFxSampler = sampler;
							}
							
				    		public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
				                super.startElement(uri, localName, name, atts);
				                
				                if (localName.equalsIgnoreCase("source")){
				                	inSource = true;
				                }else if (localName.equalsIgnoreCase("wrap_s")){
				                	// TODO: wrap_s
				                }else if (localName.equalsIgnoreCase("wrap_t")){
				                	// TODO: wrap_t				                	
				                }else if (localName.equalsIgnoreCase("minfilter")){
				                	// TODO: minfilter				                	
				                }else if (localName.equalsIgnoreCase("magfilter")){
				                	// TODO: magfilter				                	
				                }else if (localName.equalsIgnoreCase("mipfilter")){
				                	// TODO: mipfilter				                	
				                }else if (localName.equalsIgnoreCase("border_color")){
				                	// TODO: border_color				                	
				                }else if (localName.equalsIgnoreCase("mipmap_maxlevel")){
				                	// TODO: mipmap_maxlevel				                	
				                }else if (localName.equalsIgnoreCase("mipmap_bias")){
				                	// TODO: mipmap_bias				                	
				                }
				            }

				            public void endElement(String uri, String localName, String name) throws SAXException {
				                super.endElement(uri, localName, name);
				                
				                if (localName.equalsIgnoreCase("source")){
				                	inSource = false;
				                }else if (localName.equalsIgnoreCase("wrap_s")){
				                	// TODO: wrap_s
				                }else if (localName.equalsIgnoreCase("wrap_t")){
				                	// TODO: wrap_t				                	
				                }else if (localName.equalsIgnoreCase("minfilter")){
				                	// TODO: minfilter				                	
				                }else if (localName.equalsIgnoreCase("magfilter")){
				                	// TODO: magfilter				                	
				                }else if (localName.equalsIgnoreCase("mipfilter")){
				                	// TODO: mipfilter				                	
				                }else if (localName.equalsIgnoreCase("border_color")){
				                	// TODO: border_color				                	
				                }else if (localName.equalsIgnoreCase("mipmap_maxlevel")){
				                	// TODO: mipmap_maxlevel				                	
				                }else if (localName.equalsIgnoreCase("mipmap_bias")){
				                	// TODO: mipmap_bias				                	
				                }else if (localName.equalsIgnoreCase("sampler2D")){
				                    xr.setContentHandler(new colladaEffectParam(daeParam));
				                }
				            }
				            
				            public void characters(char[] ch, int start, int length) throws SAXException {
				                super.characters(ch, start, length);
				                String text = new String(ch, start, length);
				                
				                if(inSource){
				                	daeFxSampler.useSurface((ColladaFxSurface) daeProfile.getParamById(text).getParametric());
				                }
				            }
			            }
		            }

		            private class colladaEffectTechnique extends DefaultHandler{
		            	private ColladaTechniqueFx daeTech;

						public colladaEffectTechnique(ColladaTechniqueFx tech) {
							daeTech = tech;
						}

			    		public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
			                super.startElement(uri, localName, name, atts);
			                
			                if (localName.equalsIgnoreCase("phong")){
			                	xr.setContentHandler(new colladaEffectTechniquePhong(daeTech.createPhongShader()));
			                }
			            }

			            public void endElement(String uri, String localName, String name) throws SAXException {
			                super.endElement(uri, localName, name);
			                
			                if (localName.equalsIgnoreCase("technique")){
			                    xr.setContentHandler(new colladaEffectCommon(daeProfile));
			                }
			            }
			            
			            private class colladaEffectTechniquePhong extends DefaultHandler{

			            	private ColladaTechniqueFxPhong daePhong;
							private boolean inEmission = false;
							private boolean inAmbient = false;
							private boolean inDiffuse = false;
							private boolean inSpecular = false;
							private boolean inShininess = false;
							private boolean inReflective = false;
							private boolean inReflectivity = false;
							private boolean inTransparent = false;
							private boolean inTransparency = false;
							private boolean inIndexOfRefraction = false;
							private boolean isColor = false;
							private boolean isFloat = false;

							public colladaEffectTechniquePhong(ColladaTechniqueFxPhong phong) {
								daePhong = phong;
							}

				    		public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
				                super.startElement(uri, localName, name, atts);

				                if (localName.equalsIgnoreCase("color")){
				                	isColor = true;
				                }else if (localName.equalsIgnoreCase("float")){
				                	isFloat = true;
				                }else if (localName.equalsIgnoreCase("emission")){
				                	inEmission = true;
				                }else if (localName.equalsIgnoreCase("ambient")){
				                	inAmbient = true;
				                }else if (localName.equalsIgnoreCase("diffuse")){
				                	inDiffuse = true;
				                }else if (localName.equalsIgnoreCase("specular")){
				                	inSpecular = true;
				                }else if (localName.equalsIgnoreCase("shininess")){
				                	inShininess = true;
				                }else if (localName.equalsIgnoreCase("reflective")){
				                	inReflective = true;
				                }else if (localName.equalsIgnoreCase("reflectivity")){
				                	inReflectivity = true;
				                }else if (localName.equalsIgnoreCase("transparent")){
				                	inTransparent = true;
				                }else if (localName.equalsIgnoreCase("transparency")){
				                	inTransparency = true;
				                }else if (localName.equalsIgnoreCase("index_of_refraction")){
				                	inIndexOfRefraction = true;
				                }else if (inDiffuse && localName.equalsIgnoreCase("texture")){
				                	ColladaParam param = daeProfile.getParamById(atts.getValue("texture"));
				                	daePhong.setDiffuse(param,atts.getValue("texcoord"));
				                }
				            }

				            public void endElement(String uri, String localName, String name) throws SAXException {
				                super.endElement(uri, localName, name);
				                
				                if (localName.equalsIgnoreCase("color")){
				                	isColor = false;
				                }else if (localName.equalsIgnoreCase("float")){
				                	isFloat = false;
				                }else if (localName.equalsIgnoreCase("emission")){
				                	inEmission = false;
				                }else if (localName.equalsIgnoreCase("ambient")){
				                	inAmbient = false;
				                }else if (localName.equalsIgnoreCase("diffuse")){
				                	inDiffuse = false;
				                }else if (localName.equalsIgnoreCase("specular")){
				                	inSpecular = false;
				                }else if (localName.equalsIgnoreCase("shininess")){
				                	inShininess = false;
				                }else if (localName.equalsIgnoreCase("reflective")){
				                	inReflective = false;
				                }else if (localName.equalsIgnoreCase("reflectivity")){
				                	inReflectivity = false;
				                }else if (localName.equalsIgnoreCase("transparent")){
				                	inTransparent = false;
				                }else if (localName.equalsIgnoreCase("transparency")){
				                	inTransparency = false;
				                }else if (localName.equalsIgnoreCase("index_of_refraction")){
				                	inIndexOfRefraction = false;
				                }else if (localName.equalsIgnoreCase("phong")){
				                    xr.setContentHandler(new colladaEffectTechnique(daeTech));
				                }
				            }

				            public void characters(char[] ch, int start, int length) throws SAXException {
				                super.characters(ch, start, length);
				                String text = new String(ch, start, length);

				                if( isColor ){
				                	Pattern p = Pattern.compile("\\s+");
				                	String[] temp = p.split(text.trim());
				                	float x = Float.parseFloat(temp[0]);
				                	float y = Float.parseFloat(temp[1]);
				                	float z = Float.parseFloat(temp[2]);
				                	float w = Float.parseFloat(temp[3]);
				            
					                if( inEmission ){
					                	daePhong.setEmission(new ColladaVector4f(x,y,z,w));
					                }else if( inAmbient ){
					                	daePhong.setAmbient(new ColladaVector4f(x,y,z,w));
					                }else if( inSpecular ){
					                	daePhong.setSpecular(new ColladaVector4f(x,y,z,w));
					                }
				                }else if( isFloat ){
				                	float val = Float.parseFloat(text);
					                if( inShininess ){
					                	daePhong.setShininess(val);
					                }else if( inIndexOfRefraction ){
					                	daePhong.setIndexOfRefraction(val);				                	
					                }
				                }
				            }
			            }
		            }
	            }
            }
        }
        
        private class colladaLibraryMaterials extends DefaultHandler{
        	private ColladaObjects daeObj = null;
     	   
            public colladaLibraryMaterials(ColladaObjects obj) {
            	daeObj = obj;
    		}

    		public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
                super.startElement(uri, localName, name, atts);
                
                if (localName.equalsIgnoreCase("material")){
                	xr.setContentHandler(new colladaMaterial(daeObj.createMaterial(atts.getValue("id"))));
                }
            }

            public void endElement(String uri, String localName, String name) throws SAXException {
                super.endElement(uri, localName, name);
                
                if (localName.equalsIgnoreCase("library_materials")){
                    xr.setContentHandler(new colladaMain(daeObj));
                }
            }
            
            private class colladaMaterial extends DefaultHandler{
            	private ColladaMaterial daeMat;
            	
        		public colladaMaterial(ColladaMaterial mat) {
        			daeMat = mat;
				}

				public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
                    super.startElement(uri, localName, name, atts);
                    
                    if (localName.equalsIgnoreCase("instance_effect")){
                    	daeMat.useEffect(daeObj.getEffectById(objRefToObj(atts.getValue("url"))));
                    }
                }

                public void endElement(String uri, String localName, String name) throws SAXException {
                    super.endElement(uri, localName, name);
                    
                    if (localName.equalsIgnoreCase("material")){
                        xr.setContentHandler(new colladaLibraryMaterials(daeObj));
                    }
                }
            }
        }
        
        private class colladaLibraryGeometries extends DefaultHandler{
        	private ColladaObjects daeObj = null;
        	   
            public colladaLibraryGeometries(ColladaObjects obj) {
            	daeObj = obj;
    		}

    		public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
                super.startElement(uri, localName, name, atts);
                
                if (localName.equalsIgnoreCase("geometry")){
                    xr.setContentHandler(new colladaGeometry(daeObj.createGeometry(atts.getValue("id"))));
                }
            }

            public void endElement(String uri, String localName, String name) throws SAXException {
                super.endElement(uri, localName, name);
                
                if (localName.equalsIgnoreCase("library_geometries")){
                    xr.setContentHandler(new colladaMain(daeObj));
                }
            }
            
	        private class colladaGeometry extends DefaultHandler{
	        	private ColladaGeometry geoObj = null;
	            
	            public colladaGeometry(ColladaGeometry obj) {
	            	geoObj = obj;
	    		}
	
	    		public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
	                super.startElement(uri, localName, name, atts);
	                
	                if (localName.equalsIgnoreCase("source")){
	                    xr.setContentHandler(new colladaSource(geoObj.createSource(atts.getValue("id"))));
	                } else if (localName.equalsIgnoreCase("polylist")){
	                    xr.setContentHandler(new colladaPolylist(geoObj.createPolylist(atts.getValue("material"))));
	                } else if (localName.equalsIgnoreCase("triangles")){
	                    xr.setContentHandler(new colladaTriangles(geoObj.createTriangles(atts.getValue("material"))));
	                } else if (localName.equalsIgnoreCase("vertices")){
	                    xr.setContentHandler(new colladaVertices(geoObj.createVertices(atts.getValue("id"))));
	                }
	            }
	
	            public void endElement(String uri, String localName, String name) throws SAXException {
	                super.endElement(uri, localName, name);
	                
	                if (localName.equalsIgnoreCase("geometry")){
	                	xr.setContentHandler(new colladaLibraryGeometries(daeObj));                	
	                }
	            }
	            
		        private class colladaVertices extends DefaultHandler{
		        	private ColladaVertices verObj;
		            
		            public colladaVertices(ColladaVertices obj) {
		            	verObj = obj;
		    		}

		    		public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
		                super.startElement(uri, localName, name, atts);
		                
		                if(localName.equalsIgnoreCase("input")){
		                	verObj.useSource(geoObj.getSourceById(objRefToObj(atts.getValue("source"))));
		                }
		            }

		            public void endElement(String uri, String localName, String name) throws SAXException {
		                super.endElement(uri, localName, name);
		                
		                if (localName.equalsIgnoreCase("vertices")){
		                	xr.setContentHandler(new colladaGeometry(geoObj));                	
		                }
		            }
		        }
		        
		        private class colladaSource extends DefaultHandler{
		            private boolean inArray = false;
		        	private ColladaSource srcObj = null;
		        	private int arraySize = 0;
		        	private String arrayText = "";
		            
		            public colladaSource(ColladaSource obj) {
		            	srcObj = obj;
		    		}

		    		public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
		                super.startElement(uri, localName, name, atts);
		                
		                if (localName.equalsIgnoreCase("float_array")){
		                    inArray = true;
		                    arraySize = Integer.parseInt(atts.getValue("count"));
		                }
		            }

		            public void endElement(String uri, String localName, String name) throws SAXException {
		                super.endElement(uri, localName, name);
		                
		                if (localName.equalsIgnoreCase("float_array")){
		                    inArray = false;
				        	float[] arrayData = new float[arraySize];
		                	Pattern p = Pattern.compile("\\s+");
		                	String[] temp = p.split(arrayText.trim());
		                    if( (arraySize != temp.length) && (arraySize > 0) ){
		                    	throw new SAXException("Inconsistent source size for " + srcObj.getId());
		                    }

							for (int i=0; i < temp.length; i++){
								arrayData[i] = Float.parseFloat(temp[i]);
							}

							ColladaSource.FloatArray floatArray = srcObj.getFloatArray();
							floatArray.setFloatData(arrayData);
		                }else if (localName.equalsIgnoreCase("source")){
		                	xr.setContentHandler(new colladaGeometry(geoObj));                	
		                }
		            }

		            public void characters(char[] ch, int start, int length) throws SAXException {
		                super.characters(ch, start, length);
		                String text = new String(ch, start, length);

		                if (inArray){
		                	arrayText += text;
		                }
		            }
		        }
		        
		        private class colladaPolylist extends DefaultHandler{
		        	private ColladaPolylist daePolys = null;
		        	private boolean inVCount = false;
		        	private boolean inP = false;
		        	private String indxText = "";
		        	private String vcntText = "";
		            
		            public colladaPolylist(ColladaPolylist poly) {
		            	daePolys = poly;
		    		}

		    		public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
		                super.startElement(uri, localName, name, atts);

		                if (localName.equalsIgnoreCase("input")){
		                	if( atts.getValue("semantic").equalsIgnoreCase("VERTEX") ){
		                		daePolys.useVertices(
		                				geoObj.getVerticesById(objRefToObj(atts.getValue("source"))),
		                				Integer.parseInt(atts.getValue("offset")),
		                				3);
		                	}else if( atts.getValue("semantic").equalsIgnoreCase("NORMAL") ){
		                		daePolys.useNormals(
		                				geoObj.getSourceById(objRefToObj(atts.getValue("source"))),
		                				Integer.parseInt(atts.getValue("offset")),
		                				3);
		                	}else if( atts.getValue("semantic").equalsIgnoreCase("TEXCOORD") ){
		                		daePolys.useTexCoords(
		                				geoObj.getSourceById(objRefToObj(atts.getValue("source"))),
		                				Integer.parseInt(atts.getValue("offset")),
		                				2);
		                	}
		                }else if(localName.equalsIgnoreCase("vcount")){
		                	inVCount = true;
		                }else if(localName.equalsIgnoreCase("p")){
		                	inP = true;
		                }
		            }
		
		            public void endElement(String uri, String localName, String name) throws SAXException {
		                super.endElement(uri, localName, name);
		                
		                if (localName.equalsIgnoreCase("vcount")){
		                    inVCount = false;
		                    
		                	ColladaVertexCount vrtxCount = daePolys.createVertexCount();

		                	Pattern p = Pattern.compile("\\s+");
		                	String[] temp = p.split(vcntText.trim());
				        	short[] vcntData = new short[temp.length];

							for (int i=0; i < temp.length; i++){
								vcntData[i] = Short.parseShort(temp[i]);
							}

							vrtxCount.setVertexCount(vcntData);
		                }else if (localName.equalsIgnoreCase("p")){
		                    inP = false;
		                    
		                	ColladaIndices indxSrc = daePolys.createIndices();

		                	Pattern p = Pattern.compile("\\s+");
		                	String[] temp = p.split(indxText.trim());
				        	int[] indxData = new int[temp.length];

							for (int i=0; i < temp.length; i++){
								indxData[i] = Integer.parseInt(temp[i]);
							}

	                        indxSrc.setIndices(indxData);
		                }else if (localName.equalsIgnoreCase("polylist")){
		                	xr.setContentHandler(new colladaGeometry(geoObj));                	
		                }
		            }

		            public void characters(char[] ch, int start, int length) throws SAXException {
		                super.characters(ch, start, length);
		                String text = new String(ch, start, length);

		                if(inVCount){
		                	vcntText += text;
		                }else if(inP){
		                	indxText += text;
		                }
		            }
		        }
	            
		        private class colladaTriangles extends DefaultHandler{
		        	private ColladaTriangles daeTris = null;
		        	private boolean inP = false;
		        	private String indxText = "";
		            
		            public colladaTriangles(ColladaTriangles tris) {
		            	daeTris = tris;
		    		}

		    		public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
		                super.startElement(uri, localName, name, atts);

		                if (localName.equalsIgnoreCase("input")){
		                	if( atts.getValue("semantic").equalsIgnoreCase("VERTEX") ){
		                		daeTris.useVertices(
		                				geoObj.getVerticesById(objRefToObj(atts.getValue("source"))),
		                				Integer.parseInt(atts.getValue("offset")),
		                				3);
		                	}else if( atts.getValue("semantic").equalsIgnoreCase("NORMAL") ){
		                		daeTris.useNormals(
		                				geoObj.getSourceById(objRefToObj(atts.getValue("source"))),
		                				Integer.parseInt(atts.getValue("offset")),
		                				3);
		                	}else if( atts.getValue("semantic").equalsIgnoreCase("TEXCOORD") ){
		                		daeTris.useTexCoords(
		                				geoObj.getSourceById(objRefToObj(atts.getValue("source"))),
		                				Integer.parseInt(atts.getValue("offset")),
		                				2);
		                	}
		                }else if(localName.equalsIgnoreCase("p")){
		                	inP = true;
		                }
		            }
		
		            public void endElement(String uri, String localName, String name) throws SAXException {
		                super.endElement(uri, localName, name);
		                
		                if (localName.equalsIgnoreCase("p")){
		                    inP = false;
		                    
		                	ColladaIndices indxSrc = daeTris.createIndices();

		                	Pattern p = Pattern.compile("\\s+");
		                	String[] temp = p.split(indxText.trim());
				        	int[] indxData = new int[temp.length];

							for (int i=0; i < temp.length; i++){
								indxData[i] = Integer.parseInt(temp[i]);
							}

	                        indxSrc.setIndices(indxData);
		                }else if (localName.equalsIgnoreCase("triangles")){
		                	xr.setContentHandler(new colladaGeometry(geoObj));                	
		                }
		            }

		            public void characters(char[] ch, int start, int length) throws SAXException {
		                super.characters(ch, start, length);
		                String text = new String(ch, start, length);

		                if(inP){
		                	indxText += text;
		                }
		            }
	            }
	        }
        }

        private class colladaLibraryScenes extends DefaultHandler{
        	private ColladaObjects daeObj;
        	
        	public colladaLibraryScenes(ColladaObjects obj){
        		daeObj = obj;
        	}

            public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
                super.startElement(uri, localName, name, atts);
                
                if (localName.equalsIgnoreCase("visual_scene")){
                	xr.setContentHandler(new colladaVisualScene(daeObj.createScene(atts.getValue("id"))));
                }
            }

            public void endElement(String uri, String localName, String name) throws SAXException {
                super.endElement(uri, localName, name);
                
                if (localName.equalsIgnoreCase("library_visual_scenes")){
                	xr.setContentHandler(new colladaMain(daeObj));
                }
            }
            
            private class colladaVisualScene extends DefaultHandler{
            	private ColladaScene daeScene;
            	
            	public colladaVisualScene(ColladaScene scene){
            		daeScene = scene;
            	}

                public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
                    super.startElement(uri, localName, name, atts);
                    
                    if (localName.equalsIgnoreCase("node")){
                    	xr.setContentHandler(new colladaSceneNode(daeScene.createNode(atts.getValue("id"), null)));
                    }
                }

                public void endElement(String uri, String localName, String name) throws SAXException {
                    super.endElement(uri, localName, name);
                    
                    if (localName.equalsIgnoreCase("visual_scene")){
                    	xr.setContentHandler(new colladaLibraryScenes(daeObj));
                    }
                }

                private class colladaSceneNode extends DefaultHandler{
                	private boolean inRotateX = false;
                	private boolean inRotateY = false;
                	private boolean inRotateZ = false;
                	private boolean inTranslate = false;
                	private boolean inScale = false;
        			private ColladaSceneNode daeNode;
                	
                	public colladaSceneNode(ColladaSceneNode node){
                		daeNode = node;
                	}

                    public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
                        super.startElement(uri, localName, name, atts);
                        
                        if (localName.equalsIgnoreCase("rotate")){
                        	if( atts.getValue("sid").equalsIgnoreCase("rotationX") ){
                        		inRotateX = true;
                        	}else if( atts.getValue("sid").equalsIgnoreCase("rotationY") ){
                        		inRotateY = true;
                        	}else if( atts.getValue("sid").equalsIgnoreCase("rotationZ") ){
                        		inRotateZ = true;
                        	}
                        }else if (localName.equalsIgnoreCase("translate")){
                        	inTranslate = true;
                        }else if (localName.equalsIgnoreCase("scale")){
                        	inScale = true;
                        }else if (localName.equalsIgnoreCase("instance_geometry")){
                        	ColladaGeometry daeNodeGeometry = daeObj.getGeometryById(objRefToObj(atts.getValue("url")));
                        	daeNode.useGeometry(daeNodeGeometry);
                        	xr.setContentHandler(new colladaGeometryInstance(daeNodeGeometry));
                        }else if (localName.equalsIgnoreCase("instance_controller")){
                        	ColladaController daeNodeCtrl = daeObj.getControllerById(objRefToObj(atts.getValue("url")));
                        	daeNode.useController(daeNodeCtrl);
                        	xr.setContentHandler(new colladaControllerInstance(daeNodeCtrl));
                        }else if (localName.equalsIgnoreCase("node")){
                        	ColladaSceneNode child = null;
                        	if( atts.getValue("type").equalsIgnoreCase("joint") ){
                            	child = daeScene.createJoint(atts.getValue("id"), daeNode);
                        	}else{
                            	child = daeScene.createNode(atts.getValue("id"), daeNode);
                        	}
                        	daeNode.addChild(child);
                    		xr.setContentHandler(new colladaSceneNode(child));
                        }
                    }

                    public void endElement(String uri, String localName, String name) throws SAXException {
                        super.endElement(uri, localName, name);
                        
                        if (localName.equalsIgnoreCase("rotate")){
                        	if( inRotateX ){
                        		inRotateX = false;
                        	}else if( inRotateY ){
                        		inRotateY = false;
                        	}else if( inRotateZ ){
                        		inRotateZ = false;
                        	}
                        }else if (localName.equalsIgnoreCase("translate")){
                        	inTranslate = false;
                        }else if (localName.equalsIgnoreCase("scale")){
                        	inScale = false;
                        }else if (localName.equalsIgnoreCase("node")){
                        	if( daeNode.getParent() == null ){
                        		xr.setContentHandler(new colladaVisualScene(daeScene));
                        	}else{
                        		xr.setContentHandler(new colladaSceneNode(daeNode.getParent()));
                        	}
                        }
                    }

                    public void characters(char[] ch, int start, int length) throws SAXException {
                        super.characters(ch, start, length);
                        String text = new String(ch, start, length);

                        if( inRotateX ){
		                	Pattern p = Pattern.compile("\\s+");
		                	String[] temp = p.split(text.trim());
		                	float x = Float.parseFloat(temp[0]);
		                	float y = Float.parseFloat(temp[1]);
		                	float z = Float.parseFloat(temp[2]);
		                	float w = Float.parseFloat(temp[3]);
		            
                        	daeNode.setRotationX(new ColladaVector4f(x,y,z,w));
                        }else if( inRotateY ){
		                	Pattern p = Pattern.compile("\\s+");
		                	String[] temp = p.split(text.trim());
		                	float x = Float.parseFloat(temp[0]);
		                	float y = Float.parseFloat(temp[1]);
		                	float z = Float.parseFloat(temp[2]);
		                	float w = Float.parseFloat(temp[3]);

                        	daeNode.setRotationY(new ColladaVector4f(x,y,z,w));
                        }else if( inRotateZ ){
		                	Pattern p = Pattern.compile("\\s+");
		                	String[] temp = p.split(text.trim());
		                	float x = Float.parseFloat(temp[0]);
		                	float y = Float.parseFloat(temp[1]);
		                	float z = Float.parseFloat(temp[2]);
		                	float w = Float.parseFloat(temp[3]);

                        	daeNode.setRotationZ(new ColladaVector4f(x,y,z,w));
                        }else if( inTranslate ){
		                	Pattern p = Pattern.compile("\\s+");
		                	String[] temp = p.split(text.trim());
		                	float x = Float.parseFloat(temp[0]);
		                	float y = Float.parseFloat(temp[1]);
		                	float z = Float.parseFloat(temp[2]);

                        	daeNode.setTranslation(new ColladaVector3f(x,y,z));
                        }else if( inScale ){
		                	Pattern p = Pattern.compile("\\s+");
		                	String[] temp = p.split(text.trim());
		                	float x = Float.parseFloat(temp[0]);
		                	float y = Float.parseFloat(temp[1]);
		                	float z = Float.parseFloat(temp[2]);

                        	daeNode.setScale(new ColladaVector3f(x,y,z));
                        }
                    }
                    
                    private class colladaControllerInstance extends DefaultHandler{
            			private ColladaController daeNodeCtrl;
                    	private boolean inSkeleton = false;
                    	
                    	public colladaControllerInstance(ColladaController ctrl){
                    		daeNodeCtrl = ctrl;
                    	}

                        public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
                            super.startElement(uri, localName, name, atts);
                            
                            if (localName.equalsIgnoreCase("instance_material")){                        
                            	ColladaSceneNode.Material mat = daeNode.createMaterial();
                            	mat.setSymbol(daeNodeCtrl.getSkin().getGeometry().getPrimitiveById(atts.getValue("symbol")));
                            	mat.setTarget(daeObj.getMaterialById(objRefToObj(atts.getValue("target"))));
                            	xr.setContentHandler(new colladaMaterialInstance(mat));
                            }else if (localName.equalsIgnoreCase("skeleton")){
                            	inSkeleton = true;
                            }
                        }

                        public void endElement(String uri, String localName, String name) throws SAXException {
                            super.endElement(uri, localName, name);
                            
                            if (localName.equalsIgnoreCase("instance_controller")){
                            	xr.setContentHandler(new colladaSceneNode(daeNode));
                            }else if (localName.equalsIgnoreCase("skeleton")){
                            	inSkeleton = false;
                            }
                        }
                        
                        public void characters(char[] ch, int start, int length) throws SAXException {
                            super.characters(ch, start, length);
                            String text = new String(ch, start, length);
                            
                            if( inSkeleton ){
                            	daeNodeCtrl.useSkeleton(daeScene.getNodeById(objRefToObj(text)));
                            }
                        }
                    
	                    private class colladaMaterialInstance extends DefaultHandler{
	            			private ColladaSceneNode.Material daeMat;
	                    	
	                    	public colladaMaterialInstance(ColladaSceneNode.Material mat){
	                    		daeMat = mat;
	                    	}
	
	                        public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
	                            super.startElement(uri, localName, name, atts);
	                        }
	
	                        public void endElement(String uri, String localName, String name) throws SAXException {
	                            super.endElement(uri, localName, name);
	                            
	                            if (localName.equalsIgnoreCase("instance_material")){
	                            	xr.setContentHandler(new colladaControllerInstance(daeNodeCtrl));
	                            }
	                        }
	                    }
                    }
                    
                    private class colladaGeometryInstance extends DefaultHandler{
            			private ColladaGeometry daeNodeGeometry;
                    	
                    	public colladaGeometryInstance(ColladaGeometry geo){
                    		daeNodeGeometry = geo;
                    	}

                        public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
                            super.startElement(uri, localName, name, atts);
                            
                            if (localName.equalsIgnoreCase("instance_material")){                        
                            	ColladaSceneNode.Material mat = daeNode.createMaterial();
                            	mat.setSymbol(daeNodeGeometry.getPrimitiveById(atts.getValue("symbol")));
                            	mat.setTarget(daeObj.getMaterialById(objRefToObj(atts.getValue("target"))));
                            	xr.setContentHandler(new colladaMaterialInstance(mat));
                            }
                        }

                        public void endElement(String uri, String localName, String name) throws SAXException {
                            super.endElement(uri, localName, name);
                            
                            if (localName.equalsIgnoreCase("instance_geometry")){
                            	xr.setContentHandler(new colladaSceneNode(daeNode));
                            }
                        }
                    
	                    private class colladaMaterialInstance extends DefaultHandler{
	            			private ColladaSceneNode.Material daeMat;
	                    	
	                    	public colladaMaterialInstance(ColladaSceneNode.Material mat){
	                    		daeMat = mat;
	                    	}
	
	                        public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
	                            super.startElement(uri, localName, name, atts);
	                        }
	
	                        public void endElement(String uri, String localName, String name) throws SAXException {
	                            super.endElement(uri, localName, name);
	                            
	                            if (localName.equalsIgnoreCase("instance_material")){
	                            	xr.setContentHandler(new colladaGeometryInstance(daeNodeGeometry));
	                            }
	                        }
	                    }
                    }
                }
            }
        }
        
        private class colladaScene extends DefaultHandler{
        	private ColladaObjects daeObj;
        	
        	public colladaScene(ColladaObjects obj){
        		daeObj = obj;
        	}

            public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
                super.startElement(uri, localName, name, atts);
                
                if (localName.equalsIgnoreCase("instance_visual_scene")){
                	daeObj.useScene(daeObj.getSceneById(objRefToObj(atts.getValue("url"))));
                }
            }

            public void endElement(String uri, String localName, String name) throws SAXException {
                super.endElement(uri, localName, name);
                
                if (localName.equalsIgnoreCase("scene")){
                	xr.setContentHandler(new colladaMain(daeObj));
                }
            }
        }
    }
}
