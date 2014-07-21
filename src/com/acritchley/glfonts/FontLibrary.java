package com.acritchley.glfonts;

import java.util.ArrayList;

import android.content.res.AssetManager;

import com.acritchley.glcolladademo.Gl2RenderedFont;

public class FontLibrary {
	private ArrayList<FontMapping> maps = new ArrayList<FontMapping>();
	private ArrayList<FontObject> fonts = new ArrayList<FontObject>();

	public FontObject createFont(String name, AssetManager assetMgr){
		// Did we already create it?
		for(FontObject font : fonts){
			if(font.getName().equalsIgnoreCase(name)){
				return font;
			}
		}

		// Does the font exist?
		for(FontMapping map : maps){
			if(map.getName().equalsIgnoreCase(name)){
				FontObject font = new FontObject(map);
				Gl2RenderedFont gl2font = new Gl2RenderedFont();
				gl2font.setAssetManager(assetMgr);
				font.setRenderer(gl2font);
				font.buildFont();
				fonts.add(font);
				return font;
			}
		}

		// Font doesn't exist.
		return null;
	}

	public FontMapping createMapping(String name, String file, String fullfile){
		FontMapping map = new FontMapping(name, file, fullfile);
		maps.add(map);
		return map;
	}
}
