package com.pfpgames.glgraphics;

import android.content.res.AssetManager;

import com.pfpgames.glfonts.FontMapping;
import com.pfpgames.glfonts.RenderedFont;

public class Gl2RenderedFont implements RenderedFont {

	private AssetManager assetMgr;
	private Gl2RendererMapAttachment renderedMap = null;

	public Gl2RenderedFont(){
	}

	@Override
	public FontMapping.RendererMapAttachment creatMapAttachment(FontMapping cmap) {
		this.renderedMap = new Gl2RendererMapAttachment(this.assetMgr, cmap);
		return this.renderedMap;
	}

	@Override
	public void draw(float cx, float cy, float cz, float size, String text){
		this.renderedMap.draw( cx, cy, cz, size, text );
	}

	public void setAssetManager(AssetManager mgr) {
		this.assetMgr = mgr;
	}
}
