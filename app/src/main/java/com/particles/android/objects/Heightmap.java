package com.particles.android.objects;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.particles.android.data.IndexBuffer;
import com.particles.android.data.VertexBuffer;
import com.particles.android.programs.HeightmapShaderProgram;
import com.particles.android.util.Geometry;

import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glDrawElements;
import static com.particles.android.Constants.BYTES_PER_FLOAT;

/**
 * Created by liuboyuan on 2017/11/13.
 */

public class Heightmap {
    private static final int POSITION_COMPONENT_COUNT = 3;
    private final int width;
    private final int height;
    private final int numElements;
    private final VertexBuffer vertexBuffer;
    private final IndexBuffer indexBuffer;

    private static final int NORMAL_COMPONENT_COUNT = 3;
    private static final int TOTAL_COMPONENT_COUNT =
            POSITION_COMPONENT_COUNT + NORMAL_COMPONENT_COUNT;
    private static final int STRIDE =
            (POSITION_COMPONENT_COUNT + NORMAL_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    public Heightmap(Bitmap bitmap) {
        width = bitmap.getWidth();
        height = bitmap.getHeight();

        if (width * height > 65536) {
            throw new RuntimeException("Heightmap is too large for the index buffer.");
        }

        numElements = calculateNumElements();
        vertexBuffer = new VertexBuffer(loadBitmapData(bitmap));
        indexBuffer = new IndexBuffer(createIndexData());

    }

    private short[] createIndexData() {
        final short[] indexData = new short[numElements];
        int offset = 0;

        for (int row = 0; row < height - 1; row++) {
            for (int col = 0; col < width - 1; col++) {
                short topLeftIndexNum = (short) (row * width + col);
                short topRightIndexNum = (short) (row * width + col + 1);
                short bottomLeftIndexNum = (short) ((row + 1) * width + col);
                short bottomRightIndexNum = (short) ((row + 1) * width + col + 1);

                indexData[offset++] = topLeftIndexNum;
                indexData[offset++] = bottomLeftIndexNum;
                indexData[offset++] = topRightIndexNum;

                indexData[offset++] = topRightIndexNum;
                indexData[offset++] = bottomLeftIndexNum;
                indexData[offset++] = bottomRightIndexNum;
            }
        }

        return indexData;
    }

    private int calculateNumElements() {
        return (width - 1) * (height - 1) * 2 * 3;
    }

    private float[] loadBitmapData(Bitmap bitmap) {
        final int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        bitmap.recycle();

        final float[] heightmapVertices =
                new float[width * height * TOTAL_COMPONENT_COUNT];
        int offset = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                final Geometry.Point point = getPoint(pixels, row, col);
                heightmapVertices[offset++] = point.x;
                heightmapVertices[offset++] = point.y;
                heightmapVertices[offset++] = point.z;
                final Geometry.Point top = getPoint(pixels, row - 1, col);
                final Geometry.Point left = getPoint(pixels, row, col - 1);
                final Geometry.Point right = getPoint(pixels, row, col + 1);
                final Geometry.Point bottom = getPoint(pixels, row + 1, col);

                final Geometry.Vector rightToLeft = Geometry.vectorBetween(right, left);
                final Geometry.Vector topToBottom = Geometry.vectorBetween(top, bottom);
                final Geometry.Vector normal = rightToLeft.crossProduct((topToBottom).normalize());

                heightmapVertices[offset++] = normal.x;
                heightmapVertices[offset++] = normal.y;
                heightmapVertices[offset++] = normal.z;
            }
        }

        return heightmapVertices;
    }

    private Geometry.Point getPoint(int[] pixels, int row, int col) {
        float x = ((float)col / (float)(width - 1)) - 0.5f;
        float z = ((float)row / (float)(height - 1)) - 0.5f;

        row = clamp(row, 0, width - 1);
        col = clamp(col, 0, height - 1);

        float y = (float) Color.red(pixels[(row * height) + col]) / (float)255;

        return new Geometry.Point(x, y, z);
    }

    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    public void bindData(HeightmapShaderProgram heightmapProgram) {
        vertexBuffer.setVertexAttribPointer(0,
                heightmapProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT, STRIDE);
        vertexBuffer.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT * BYTES_PER_FLOAT,
                heightmapProgram.getNormalAttributeLocation(),
                NORMAL_COMPONENT_COUNT, STRIDE
        );
    }

    public void draw() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getBufferId());
        glDrawElements(GL_TRIANGLES, numElements, GL_UNSIGNED_SHORT, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }
}
