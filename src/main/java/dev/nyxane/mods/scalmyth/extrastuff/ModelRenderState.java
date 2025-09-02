package dev.nyxane.mods.scalmyth.extrastuff;

public class ModelRenderState {
    public static ModelRenderState current;

    public float[] positions;
    public float[] normals;
    public float[] texCoords;
    public int[] indices;
    public String diffuseTexturePath;
    public org.joml.Matrix4f transformM4;
}
