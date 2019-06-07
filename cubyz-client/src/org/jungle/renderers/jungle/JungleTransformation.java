package org.jungle.renderers.jungle;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jungle.Camera;
import org.jungle.Spatial;
import org.jungle.renderers.Transformation;

public class JungleTransformation extends Transformation {

    private final Matrix4f projectionMatrix;

    private final Matrix4f worldMatrix;
    
    private final Matrix4f viewMatrix;
    
    private final Matrix4f modelViewMatrix;
    
    private final Matrix4f orthoMatrix;
    
    private static final float DEGTORAD = (float)(Math.PI/180); // Makes the results of toRadians slightly less accurate, but reduces the number of type conversions.

    private static final Vector3f xVec = new Vector3f(1, 0, 0); // There is no need to create  a new object every time this is needed.
    private static final Vector3f yVec = new Vector3f(0, 1, 0);

    public JungleTransformation() {
        worldMatrix = new Matrix4f();
        projectionMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
        modelViewMatrix = new Matrix4f();
        orthoMatrix = new Matrix4f();
    }

    public final Matrix4f getProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
        float aspectRatio = width / height;        
        projectionMatrix.identity();
        projectionMatrix.perspective(fov, aspectRatio, zNear, zFar);
        return projectionMatrix;
    }

    public Matrix4f getWorldMatrix(Vector3f offset, Vector3f rotation, float scale) {
        worldMatrix.identity().translate(offset).
		        rotateX(DEGTORAD*rotation.x).
		        rotateY(DEGTORAD*rotation.y).
		        rotateZ(DEGTORAD*rotation.z).
                scale(scale);
        return worldMatrix;
    }
    
    public final Matrix4f getOrthoProjectionMatrix(float left, float right, float bottom, float top) {
        orthoMatrix.setOrtho2D(left, right, bottom, top);
        return orthoMatrix;
    }
    
    public Matrix4f getOrtoProjModelMatrix(Spatial gameItem, Matrix4f orthoMatrix) {
        Vector3f rotation = gameItem.getRotation();
        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.identity().translate(gameItem.getPosition()).
		        rotateX(-DEGTORAD*rotation.x).
		        rotateY(-DEGTORAD*rotation.y).
		        rotateZ(-DEGTORAD*rotation.z).
                scale(gameItem.getScale());
        Matrix4f orthoMatrixCurr = new Matrix4f(orthoMatrix);
        orthoMatrixCurr.mul(modelMatrix);
        return orthoMatrixCurr;
    }
    
    public Matrix4f getViewMatrix(Camera camera) {
        Vector3f cameraPos = camera.getPosition();
        Vector3f rotation = camera.getRotation();

        viewMatrix.identity();
        // First do the rotation so camera rotates over its position
        viewMatrix.rotate(DEGTORAD*rotation.x, xVec)
            .rotate(DEGTORAD*rotation.y, yVec);
        // Then do the translation
        viewMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        return viewMatrix;
    }
    
    public Matrix4f getModelViewMatrix(Spatial spatial, Matrix4f viewMatrix) {
        Vector3f rotation = spatial.getRotation();
        modelViewMatrix.identity().translate(spatial.getPosition()).
            rotateX(-DEGTORAD*rotation.x).
            rotateY(-DEGTORAD*rotation.y).
            rotateZ(-DEGTORAD*rotation.z).
            scale(spatial.getScale());
        Matrix4f viewCurr = new Matrix4f(viewMatrix);
        return viewCurr.mul(modelViewMatrix);
    }
}