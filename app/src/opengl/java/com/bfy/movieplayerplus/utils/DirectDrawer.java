package com.bfy.movieplayerplus.utils;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.bfy.movieplayerplus.MyApplication;
import com.bfy.movieplayerplus.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　彩讯科技股份有限公司
 * @company    : 彩讯科技股份有限公司
 * @author     : OuyangJinfu
 * @e-mail     : ouyangjinfu@richinfo.cn
 * @createDate : 2017/7/12 0012
 * @modifyDate : 2017/7/12 0012
 * @version    : 1.0
 * @desc       :
 * </pre>
 */

public class DirectDrawer {
    // 顶点缓存
    private FloatBuffer vertexBuffer;
    // 纹理坐标映射缓存
    private FloatBuffer mTextureCoordsBuffer;
    // 绘制顺序缓存
    private ShortBuffer drawListBuffer;
    // OpenGL 可执行程序
    private final int mProgram;
    private int mPositionHandle;
    private int mTextureCoordHandle;
    private int mMVPMatrixHandle;
    private int mTexSamplerHandle;
    private final int uSTMMatrixHandle;

    // 绘制顶点的顺序
    private short drawOrder[] = {0, 2, 1, 0, 3, 2};

    // 每个顶点的坐标数
    private final int COORDS_PER_VERTEX = 2;

    // 每个坐标数4 bytes，那么每个顶点占8 bytes
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private float mVertices[] = new float[8]/*{
        1f,-1f,
        -1f,-1f,
        1f,1f,
        -1f,1f,
    }*/;

    private float mTextureCoords[] = new float[8]/*{
        1f,0f,
        0f,0f,
        1f,1f,
        0f,1f
    }*/;
    public float[] mMVP = new float[16];

    public float[] mSTMatrix = new float[16];

    public void resetMatrix() {
        mat4f_LoadOrtho(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f, mMVP);
    }


    public DirectDrawer(int texture) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // Bind the texture handle to the 2D texture target.
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture);

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        String vertextShader = TextResourceReader.readTextFileFromResource(MyApplication.getContext()
                , R.raw.video_vertex_shader);
        String fragmentShader = TextResourceReader.readTextFileFromResource(MyApplication.getContext()
                , R.raw.video_normal_fragment_shader);

        // 创建 vertex shader和fragment shader 并将其添加到shader进行编译
        mProgram = GlUtil.createProgram(vertextShader, fragmentShader);

        if (mProgram == 0) {
            throw new RuntimeException("Unable to create program");
        }

        // 获取指向vertex shader的成员vPosition的句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GlUtil.checkLocation(mPositionHandle, "vPosition");

        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        GlUtil.checkLocation(mTextureCoordHandle, "inputTextureCoordinate");

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GlUtil.checkLocation(mMVPMatrixHandle, "uMVPMatrix");

        uSTMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
        GlUtil.checkLocation(uSTMMatrixHandle, "uSTMatrix");

        mTexSamplerHandle =  GLES20.glGetUniformLocation(mProgram, "s_texture");
        GlUtil.checkLocation(mMVPMatrixHandle, "s_texture");

        // initialize vertex byte buffer for shape coordinates
        updateVertices();

        setTexCoords();

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        mat4f_LoadOrtho(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f, mMVP);
    }

    public void draw() {
        // 将program添加到OpenGL ES环境中
        GLES20.glUseProgram(mProgram);
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture);

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
        GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, mTextureCoordsBuffer);

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVP, 0);
        GLES20.glUniformMatrix4fv(uSTMMatrixHandle, 1, false, mSTMatrix, 0);
        GLES20.glUniform1i(mTexSamplerHandle, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);


        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
//        GLES20.glDisableVertexAttribArray(mMVPMatrixHandle);
//        GLES20.glDisableVertexAttribArray(mTexSamplerHandle);
//        GLES20.glDisableVertexAttribArray(uSTMMatrixHandle);
    }

    public static void mat4f_LoadOrtho(float left, float right, float bottom, float top, float near, float far, float[] mout) {
        float r_l = right - left;
        float t_b = top - bottom;
        float f_n = far - near;
        float tx = -(right + left) / (right - left);
        float ty = -(top + bottom) / (top - bottom);
        float tz = -(far + near) / (far - near);

        mout[0] = 2.0f / r_l;
        mout[1] = 0.0f;
        mout[2] = 0.0f;
        mout[3] = 0.0f;

        mout[4] = 0.0f;
        mout[5] = 2.0f / t_b;
        mout[6] = 0.0f;
        mout[7] = 0.0f;

        mout[8] = 0.0f;
        mout[9] = 0.0f;
        mout[10] = -2.0f / f_n;
        mout[11] = 0.0f;

        mout[12] = tx;
        mout[13] = ty;
        mout[14] = tz;
        mout[15] = 1.0f;
    }

    public void updateVertices() {
        final float w = 1.0f;
        final float h = 1.0f;
        mVertices[0] = -w;
        mVertices[1] = h;
        mVertices[2] = -w;
        mVertices[3] = -h;
        mVertices[4] = w;
        mVertices[5] = -h;
        mVertices[6] = w;
        mVertices[7] = h;
        vertexBuffer = ByteBuffer.allocateDirect(mVertices.length * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer().put(mVertices);
        vertexBuffer.position(0);
    }

    public void setTexCoords() {
        mTextureCoords[0] = 0;
        mTextureCoords[1] = 1 ;
        mTextureCoords[2] = 0;
        mTextureCoords[3] = 0 ;
        mTextureCoords[4] = 1;
        mTextureCoords[5] = 0 ;
        mTextureCoords[6] = 1;
        mTextureCoords[7] = 1 ;
        mTextureCoordsBuffer = ByteBuffer.allocateDirect(mTextureCoords.length * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer().put(mTextureCoords);
        mTextureCoordsBuffer.position(0);
    }
}
