package com.pfpgames.collada;

public class ColladaTriangles implements ColladaPrimitive {

    private ColladaVertices dae_vertices = null;
    private int vertices_offset = 0;
    private ColladaSource dae_normals = null;
    private int normals_offset = 0;
    private ColladaSource dae_texcoords = null;
    private int texCoords_offset = 0;
    private int num_inputs = 0;
    private int vertices_stride = 0;
    private int normals_stride = 0;
    private int texCoords_stride = 0;
    private String id;
    private ColladaIndices dae_indices = null;

    public ColladaTriangles(String sid) {
        id = sid;
    }

    @Override
    public String getId(){
        return id;
    }

    @Override
    public ColladaIndices createIndices() {
        dae_indices = new ColladaIndices();
        return dae_indices;
    }

    @Override
    public void useVertices(ColladaVertices verts, int offset, int stride){
        dae_vertices = verts;
        vertices_offset = offset;
        vertices_stride = stride;
        num_inputs++;
    }

    @Override
    public void useNormals(ColladaSource norms, int offset, int stride){
        dae_normals = norms;
        normals_offset = offset;
        normals_stride = stride;
        num_inputs++;
    }

    @Override
    public void useTexCoords(ColladaSource texcoords, int offset, int stride){
        dae_texcoords  = texcoords;
        texCoords_offset = offset;
        texCoords_stride = stride;
        num_inputs++;
    }

    @Override
    public ColladaMesh constructMesh(ColladaMesh mesh, boolean trans_uv, boolean flip_u, boolean flip_v){
        mesh.setId(id);

        int[]   src_indices = dae_indices.getIndices();
        float[] src_vertices = dae_vertices.getSource().getFloatArray().getFloatData();
        float[] src_normals = null;
        float[] src_texCoords = null;
        int totalIndices = 0;

        int[]   dst_indices = null;
        float[] dst_vertices = null;
        float[] dst_normals = null;
        float[] dst_texCoords = null;

        float minx;
        float maxx;
        float miny;
        float maxy;
        float minz;
        float maxz;

        totalIndices = src_indices.length/num_inputs;

        dst_indices = new int[totalIndices];
        dst_vertices = new float[totalIndices*vertices_stride];

        if( dae_normals != null){
            src_normals = dae_normals.getFloatArray().getFloatData();
            dst_normals = new float[totalIndices*normals_stride];
        }

        if( dae_texcoords != null ){
            src_texCoords = dae_texcoords.getFloatArray().getFloatData();
            dst_texCoords = new float[totalIndices*texCoords_stride];
        }

        minx = src_vertices[0];
        maxx = src_vertices[0];
        miny = src_vertices[1];
        maxy = src_vertices[1];
        minz = src_vertices[2];
        maxz = src_vertices[2];

        int f=0;
        int v=0; // vector offset into dst
        int t=0; // tex offset into dst
        for( int x=0; x < src_indices.length; x += 3*num_inputs)
        {
            // The indices for normals and vertices are interleaved in COLLADA.
            dst_vertices[v]   = src_vertices[src_indices[x+vertices_offset]*vertices_stride];
            dst_vertices[v+1] = src_vertices[src_indices[x+vertices_offset]*vertices_stride+1];
            dst_vertices[v+2] = src_vertices[src_indices[x+vertices_offset]*vertices_stride+2];
            dst_indices[f] = f;

            minx = Math.min( minx, dst_vertices[v] );
            maxx = Math.max( maxx, dst_vertices[v] );
            miny = Math.min( miny, dst_vertices[v+1] );
            maxy = Math.max( maxy, dst_vertices[v+1] );
            minz = Math.min( minz, dst_vertices[v+2] );
            maxz = Math.max( maxz, dst_vertices[v+2] );

            dst_vertices[v+3]   = src_vertices[src_indices[x+num_inputs+vertices_offset]*vertices_stride];
            dst_vertices[v+4] = src_vertices[src_indices[x+num_inputs+vertices_offset]*vertices_stride+1];
            dst_vertices[v+5] = src_vertices[src_indices[x+num_inputs+vertices_offset]*vertices_stride+2];
            dst_indices[f+1] = f+1;

            minx = Math.min( minx, dst_vertices[v+3] );
            maxx = Math.max( maxx, dst_vertices[v+3] );
            miny = Math.min( miny, dst_vertices[v+4] );
            maxy = Math.max( maxy, dst_vertices[v+4] );
            minz = Math.min( minz, dst_vertices[v+5] );
            maxz = Math.max( maxz, dst_vertices[v+5] );

            dst_vertices[v+6]   = src_vertices[src_indices[x+2*num_inputs+vertices_offset]*vertices_stride];
            dst_vertices[v+7] = src_vertices[src_indices[x+2*num_inputs+vertices_offset]*vertices_stride+1];
            dst_vertices[v+8] = src_vertices[src_indices[x+2*num_inputs+vertices_offset]*vertices_stride+2];
            dst_indices[f+2] = f+2;

            minx = Math.min( minx, dst_vertices[v+6] );
            maxx = Math.max( maxx, dst_vertices[v+6] );
            miny = Math.min( miny, dst_vertices[v+7] );
            maxy = Math.max( maxy, dst_vertices[v+7] );
            minz = Math.min( minz, dst_vertices[v+8] );
            maxz = Math.max( maxz, dst_vertices[v+8] );

            if(src_normals != null){
                dst_normals[v]   = src_normals[src_indices[x+normals_offset]*normals_stride];
                dst_normals[v+1] = src_normals[src_indices[x+normals_offset]*normals_stride+1];
                dst_normals[v+2] = src_normals[src_indices[x+normals_offset]*normals_stride+2];

                dst_normals[v+3] = src_normals[src_indices[x+num_inputs+normals_offset]*normals_stride];
                dst_normals[v+4] = src_normals[src_indices[x+num_inputs+normals_offset]*normals_stride+1];
                dst_normals[v+5] = src_normals[src_indices[x+num_inputs+normals_offset]*normals_stride+2];

                dst_normals[v+6] = src_normals[src_indices[x+2*num_inputs+normals_offset]*normals_stride];
                dst_normals[v+7] = src_normals[src_indices[x+2*num_inputs+normals_offset]*normals_stride+1];
                dst_normals[v+8] = src_normals[src_indices[x+2*num_inputs+normals_offset]*normals_stride+2];
            }

            if(src_texCoords != null){
                if(trans_uv) {
                    dst_texCoords[t + 1] = src_texCoords[src_indices[x + texCoords_offset] * texCoords_stride];
                    dst_texCoords[t] = src_texCoords[src_indices[x + texCoords_offset] * texCoords_stride + 1];

                    dst_texCoords[t + 3] = src_texCoords[src_indices[x + num_inputs + texCoords_offset] * texCoords_stride];
                    dst_texCoords[t + 2] = src_texCoords[src_indices[x + num_inputs + texCoords_offset] * texCoords_stride + 1];

                    dst_texCoords[t + 5] = src_texCoords[src_indices[x + 2 * num_inputs + texCoords_offset] * texCoords_stride];
                    dst_texCoords[t + 4] = src_texCoords[src_indices[x + 2 * num_inputs + texCoords_offset] * texCoords_stride + 1];
                }else{
                    dst_texCoords[t] = src_texCoords[src_indices[x + texCoords_offset] * texCoords_stride];
                    dst_texCoords[t + 1] = src_texCoords[src_indices[x + texCoords_offset] * texCoords_stride + 1];

                    dst_texCoords[t + 2] = src_texCoords[src_indices[x + num_inputs + texCoords_offset] * texCoords_stride];
                    dst_texCoords[t + 3] = src_texCoords[src_indices[x + num_inputs + texCoords_offset] * texCoords_stride + 1];

                    dst_texCoords[t + 4] = src_texCoords[src_indices[x + 2 * num_inputs + texCoords_offset] * texCoords_stride];
                    dst_texCoords[t + 5] = src_texCoords[src_indices[x + 2 * num_inputs + texCoords_offset] * texCoords_stride + 1];
                }

                if(flip_u){
                    dst_texCoords[t] = 1 - dst_texCoords[t];
                    dst_texCoords[t + 2] = 1 - dst_texCoords[t + 2];
                    dst_texCoords[t + 4] = 1 - dst_texCoords[t + 4];
                }

                if(flip_v){
                    dst_texCoords[t + 1] = 1 - dst_texCoords[t + 1];
                    dst_texCoords[t + 3] = 1 - dst_texCoords[t + 3];
                    dst_texCoords[t + 5] = 1 - dst_texCoords[t + 5];
                }
            }

            v += 3*3;
            t += 3*2;
            f += 3;
        }

        mesh.setIndices(dst_indices);
        mesh.setVertices(dst_vertices);

        if(dst_normals != null){
            mesh.setNormals(dst_normals);
        }

        if(dst_texCoords != null){
            mesh.setTexCoords(dst_texCoords);
        }

        mesh.setBounds(minx, maxx, miny, maxy, minz, maxz);

        return mesh;
    }
}
