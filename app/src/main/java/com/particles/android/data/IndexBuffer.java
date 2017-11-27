package com.particles.android.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glGenBuffers;
import static com.particles.android.Constants.BYTES_PER_SHORT;

/**
 * Created by liuboyuan on 2017/11/13.
 */

public class IndexBuffer {
    private final int bufferId;

    public IndexBuffer(short[] vertexData) {
        final int buffers[] = new int[1];
        glGenBuffers(buffers.length, buffers, 0);
        if (buffers[0] == 0) {
            throw new RuntimeException("Could not create a new vertex buffer object.");
        }
        bufferId = buffers[0];

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffers[0]);

        ShortBuffer vertexArray = ByteBuffer
                .allocateDirect(vertexData.length * BYTES_PER_SHORT)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(vertexData);

        vertexArray.position(0);

        glBufferData(GL_ELEMENT_ARRAY_BUFFER, vertexArray.capacity() * BYTES_PER_SHORT,
                vertexArray, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public int getBufferId() {
        return bufferId;
    }

}
