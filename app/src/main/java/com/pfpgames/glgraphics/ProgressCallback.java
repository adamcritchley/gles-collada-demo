package com.pfpgames.glgraphics;

public abstract class ProgressCallback{
    private int callback_frequency;

    public ProgressCallback(int cbfreq) {
        callback_frequency = cbfreq;
    }

    public enum PROGRESS_STAGE{
        PRELOAD, LOADING, PARSING, BUILDING, POSTLOAD
    }

    public abstract boolean reportProgress(PROGRESS_STAGE stage, int progress, int step, int total);

    public abstract class DataParser {
        public abstract void setValue(int i, String s);

        public void parse(String[] str_values) {
            for( int i=0; i<str_values.length; i++ ){
                setValue(i, str_values[i]);
                reportProgress(PROGRESS_STAGE.PARSING, i, 1, str_values.length);
            }
        }
    }
}
