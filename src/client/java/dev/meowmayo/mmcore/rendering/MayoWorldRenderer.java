package dev.meowmayo.mmcore.rendering;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MayoWorldRenderer {
    private static final ConcurrentHashMap<Integer, BoxData> BOXES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, TextData> TEXTS = new ConcurrentHashMap<>();
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    private static final ByteBufferBuilder FILLED_ALLOC = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final ByteBufferBuilder SKELETON_ALLOC = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final ByteBufferBuilder FILLED_ESP_ALLOC = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final ByteBufferBuilder SKELETON_ESP_ALLOC = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);

    private static MappableRingBuffer ringFilled;
    private static MappableRingBuffer ringSkeleton;
    private static MappableRingBuffer ringFilledEsp;
    private static MappableRingBuffer ringSkeletonEsp;

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();

    public static int addBox(double x, double y, double z, float size, int color, boolean filled, boolean esp) {
        int id = ID_GENERATOR.getAndIncrement();
        BOXES.put(id, new BoxData(x, y, z, size, color, filled, esp));
        return id;
    }

    public static int addText(double x, double y, double z, float scale, String text) {
        int id = ID_GENERATOR.getAndIncrement();
        TEXTS.put(id, new TextData(x, y, z, scale, text));
        return id;
    }

    public static void moveBox(int id, double newX, double newY, double newZ) {
        BoxData box = BOXES.get(id);
        if (box != null) { box.x = newX; box.y = newY; box.z = newZ; }
    }

    public static void moveText(int id, double newX, double newY, double newZ) {
        TextData text = TEXTS.get(id);
        if (text != null) { text.x = newX; text.y = newY; text.z = newZ; }
    }

    public static void translateBox(int id, double pushX, double pushY, double pushZ) {
        BoxData box = BOXES.get(id);
        if (box != null) { box.x += pushX; box.y += pushY; box.z += pushZ; }
    }

    public static void translateText(int id, double pushX, double pushY, double pushZ) {
        TextData text = TEXTS.get(id);
        if (text != null) { text.x += pushX; text.y += pushY; text.z += pushZ; }
    }

    public static void resizeBox(int id, float newSize) {
        BoxData box = BOXES.get(id);
        if (box != null) box.size = newSize;
    }

    public static void changeText(int id, String newText) {
        TextData text = TEXTS.get(id);
        if (text != null) { text.text = newText; }
    }

    public static void removeBox(int id) { BOXES.remove(id); }

    public static void removeText(int id) { TEXTS.remove(id); }

    public static void init() {
        LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN.register(context -> {
            if (BOXES.isEmpty()) return;

            Vec3 camera = context.levelState().cameraRenderState.pos;
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

            MeshData fMesh = prepare(FILLED_ALLOC, MayoRenderPipeline.FILLED_BOX, true, false, camera);
            MeshData sMesh = prepare(SKELETON_ALLOC, MayoRenderPipeline.SKELETON_BOX, false, false, camera);
            MeshData fEspMesh = prepare(FILLED_ESP_ALLOC, MayoRenderPipeline.FILLED_BOX_ESP, true, true, camera);
            MeshData sEspMesh = prepare(SKELETON_ESP_ALLOC, MayoRenderPipeline.SKELETON_BOX_ESP, false, true, camera);

            if (fMesh != null) upload(fMesh, 0);
            if (sMesh != null) upload(sMesh, 1);
            if (fEspMesh != null) upload(fEspMesh, 2);
            if (sEspMesh != null) upload(sEspMesh, 3);

            GpuBufferSlice transforms = RenderSystem.getDynamicUniforms()
                    .writeTransform(RenderSystem.getModelViewMatrixCopy(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

            try (RenderPass pass = encoder.createRenderPass(() -> "Waypoints Pass",
                    Minecraft.getInstance().gameRenderer.mainRenderTarget().getColorTextureView(), Optional.empty(),
                    Minecraft.getInstance().gameRenderer.mainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {

                RenderSystem.bindDefaultUniforms(pass);

                if (fMesh != null) draw(pass, MayoRenderPipeline.FILLED_BOX, fMesh, transforms, 0);
                if (sMesh != null) draw(pass, MayoRenderPipeline.SKELETON_BOX, sMesh, transforms, 1);
                if (fEspMesh != null) draw(pass, MayoRenderPipeline.FILLED_BOX_ESP, fEspMesh, transforms, 2);
                if (sEspMesh != null) draw(pass, MayoRenderPipeline.SKELETON_BOX_ESP, sEspMesh, transforms, 3);
            }

            if (ringFilled != null) ringFilled.rotate();
            if (ringSkeleton != null) ringSkeleton.rotate();
            if (ringFilledEsp != null) ringFilledEsp.rotate();
            if (ringSkeletonEsp != null) ringSkeletonEsp.rotate();

            if (fMesh != null) fMesh.close();
            if (sMesh != null) sMesh.close();
            if (fEspMesh != null) fEspMesh.close();
            if (sEspMesh != null) sEspMesh.close();
        });

        LevelRenderEvents.AFTER_SOLID_FEATURES.register(context -> {
            if (TEXTS.isEmpty()) return;
            for (TextData text : TEXTS.values()) {
                drawWorldText(context, text.text, text.x, text.y, text.z, text.scale);
            }
        });
    }

    public static void drawWorldText(LevelRenderContext context, String text, double x, double y, double z, float scale) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 camera = context.levelState().cameraRenderState.pos;
        PoseStack matrices = context.poseStack();

        matrices.pushPose();

        matrices.translate((float) (x - camera.x), (float) (y - camera.y), (float) (z - camera.z));
        matrices.mulPose(context.levelState().cameraRenderState.orientation);

        float scaleFactor = scale * 0.2f;
        matrices.scale(scaleFactor, -scaleFactor, scaleFactor);

        Font.PreparedText prep = mc.font.prepareText(
                text,
                -mc.font.width(text) / 2f, 0f,
                -1,
                true,
                0
        );

        net.minecraft.client.renderer.OrderedSubmitNodeCollector collector = context.submitNodeCollector();
        prep.visit(new Font.GlyphVisitor() {
            @Override
            public void acceptGlyph(TextRenderable.Styled glyph) {
                collector.submitText(
                        matrices,
                        -mc.font.width(text) / 2f,
                        0f,
                        net.minecraft.network.chat.Component.literal(text).getVisualOrderText(),
                        true,
                        Font.DisplayMode.SEE_THROUGH,
                        15728880,
                        -1,
                        0,
                        0
                );
            }
        });

        matrices.popPose();
    }

    private static MeshData prepare(ByteBufferBuilder alloc, RenderPipeline pipe, boolean filled, boolean esp, Vec3 camera) {
        BufferBuilder b = new BufferBuilder(alloc, PrimitiveTopology.QUADS, pipe.getVertexFormatBinding(0));
        boolean any = false;
        for (BoxData box : BOXES.values()) {
            if (box.filled == filled && box.esp == esp) {
                addBoxVertices(b, (float)(box.x - camera.x), (float)(box.y - camera.y), (float)(box.z - camera.z), box);
                any = true;
            }
        }
        return any ? b.build() : null;
    }

    private static void upload(MeshData mesh, int type) {
        int size = mesh.drawState().vertexCount() * mesh.drawState().format().getVertexSize();
        MappableRingBuffer ring = getRing(type, size);

        try (GpuBufferSlice.MappedView view = ring.currentBuffer().slice(0,size).map(false, true)) {
            org.lwjgl.system.MemoryUtil.memCopy(mesh.vertexBuffer(), view.data());
        }
    }

    private static MappableRingBuffer getRing(int type, int size) {
        MappableRingBuffer r = switch (type) {
            case 0 -> ringFilled;
            case 1 -> ringSkeleton;
            case 2 -> ringFilledEsp;
            default -> ringSkeletonEsp;
        };

        if (r == null || r.size() < size) {
            if (r != null) r.close();
            r = new MappableRingBuffer(() -> "ring_" + type, GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, Math.max(size, 65536));
            switch (type) {
                case 0 -> ringFilled = r;
                case 1 -> ringSkeleton = r;
                case 2 -> ringFilledEsp = r;
                default -> ringSkeletonEsp = r;
            }
        }
        return r;
    }

    private static void draw(RenderPass pass, RenderPipeline pipe, MeshData mesh, GpuBufferSlice transforms, int type) {
        int indexCount = mesh.drawState().indexCount();
        int vertexByteSize = mesh.drawState().vertexCount() * mesh.drawState().format().getVertexSize();
        MappableRingBuffer ring = getRing(type, vertexByteSize);

        pass.setPipeline(pipe);

        RenderSystem.bindDefaultUniforms(pass);
        pass.setUniform("DynamicTransforms", transforms);
        pass.setVertexBuffer(0, ring.currentBuffer().slice(0, vertexByteSize));

        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(mesh.drawState().primitiveTopology());
        pass.setIndexBuffer(indices.getBuffer(mesh.drawState().indexCount()), indices.type());

        pass.drawIndexed(indexCount, 1, 0, 0, 0);
    }

    private static void addBoxVertices(BufferBuilder b, float rx, float ry, float rz, BoxData box) {
        float h = (box.size / 2f) + 0.001f;
        float x0 = rx-h, y0 = ry-h, z0 = rz-h, x1 = rx+h, y1 = ry+h, z1 = rz+h;
        float r = (box.color >> 16 & 255) / 255f, g = (box.color >> 8 & 255) / 255f, bl = (box.color & 255) / 255f, a = (box.color >> 24 & 255) / 255f;

        if (box.filled) {
            drawSolidBox(b, x0, y0, z0, x1, y1, z1, r, g, bl, a);
        } else {
            float t = 0.02f;
            drawXEdge(b, x0,y0,z0, x1,y0,z0, t,r,g,bl,a, "X"); drawXEdge(b, x0,y0,z1, x1,y0,z1, t,r,g,bl,a, "X");
            drawXEdge(b, x0,y0,z0, x0,y0,z1, t,r,g,bl,a, "Z"); drawXEdge(b, x1,y0,z0, x1,y0,z1, t,r,g,bl,a, "Z");
            drawXEdge(b, x0,y1,z0, x1,y1,z0, t,r,g,bl,a, "X"); drawXEdge(b, x0,y1,z1, x1,y1,z1, t,r,g,bl,a, "X");
            drawXEdge(b, x0,y1,z0, x0,y1,z1, t,r,g,bl,a, "Z"); drawXEdge(b, x1,y1,z0, x1,y1,z1, t,r,g,bl,a, "Z");
            drawXEdge(b, x0,y0,z0, x0,y1,z0, t,r,g,bl,a, "Y"); drawXEdge(b, x1,y0,z0, x1,y1,z0, t,r,g,bl,a, "Y");
            drawXEdge(b, x0,y0,z1, x0,y1,z1, t,r,g,bl,a, "Y"); drawXEdge(b, x1,y0,z1, x1,y1,z1, t,r,g,bl,a, "Y");
        }
    }

    private static void drawXEdge(BufferBuilder b, float x1, float y1, float z1, float x2, float y2, float z2, float t, float r, float g, float bl, float a, String axis) {
        float sX = Math.signum(x1 + x2), sY = Math.signum(y1 + y2), sZ = Math.signum(z1 + z2);
        float hT = t / 2f, ox1=0, oy1=0, oz1=0, ox2=0, oy2=0, oz2=0;
        if (axis.equals("X")) { oy1 = hT*-sY; oz1 = hT*-sZ; oy2 = hT*sY; oz2 = hT*-sZ; }
        else if (axis.equals("Y")) { ox1 = hT*-sX; oz1 = hT*-sZ; ox2 = hT*-sX; oz2 = hT*sZ; }
        else { ox1 = hT*-sX; oy1 = hT*-sY; ox2 = hT*sX; oy2 = hT*-sY; }
        b.addVertex(x1-ox1, y1-oy1, z1-oz1).setColor(r,g,bl,a); b.addVertex(x2-ox1, y2-oy1, z2-oz1).setColor(r,g,bl,a);
        b.addVertex(x2+ox1, y2+oy1, z2+oz1).setColor(r,g,bl,a); b.addVertex(x1+ox1, y1+oy1, z1+oz1).setColor(r,g,bl,a);
        b.addVertex(x1-ox2, y1-oy2, z1-oz2).setColor(r,g,bl,a); b.addVertex(x2-ox2, y2-oy2, z2-oz2).setColor(r,g,bl,a);
        b.addVertex(x2+ox2, y2+oy2, z2+oz2).setColor(r,g,bl,a); b.addVertex(x1+ox2, y1+oy2, z1+oz2).setColor(r,g,bl,a);
    }

    private static void drawSolidBox(BufferBuilder b, float x0, float y0, float z0, float x1, float y1, float z1, float r, float g, float bl, float a) {
        b.addVertex(x0,y1,z0).setColor(r,g,bl,a); b.addVertex(x0,y1,z1).setColor(r,g,bl,a); b.addVertex(x1,y1,z1).setColor(r,g,bl,a); b.addVertex(x1,y1,z0).setColor(r,g,bl,a);
        b.addVertex(x0,y0,z0).setColor(r,g,bl,a); b.addVertex(x1,y0,z0).setColor(r,g,bl,a); b.addVertex(x1,y0,z1).setColor(r,g,bl,a); b.addVertex(x0,y0,z1).setColor(r,g,bl,a);
        b.addVertex(x0,y0,z0).setColor(r,g,bl,a); b.addVertex(x0,y1,z0).setColor(r,g,bl,a); b.addVertex(x1,y1,z0).setColor(r,g,bl,a); b.addVertex(x1,y0,z0).setColor(r,g,bl,a);
        b.addVertex(x0,y0,z1).setColor(r,g,bl,a); b.addVertex(x1,y0,z1).setColor(r,g,bl,a); b.addVertex(x1,y1,z1).setColor(r,g,bl,a); b.addVertex(x0,y1,z1).setColor(r,g,bl,a);
        b.addVertex(x0,y0,z0).setColor(r,g,bl,a); b.addVertex(x0,y0,z1).setColor(r,g,bl,a); b.addVertex(x0,y1,z1).setColor(r,g,bl,a); b.addVertex(x0,y1,z0).setColor(r,g,bl,a);
        b.addVertex(x1,y0,z0).setColor(r,g,bl,a); b.addVertex(x1,y1,z0).setColor(r,g,bl,a); b.addVertex(x1,y1,z1).setColor(r,g,bl,a); b.addVertex(x1,y0,z1).setColor(r,g,bl,a);
    }
}