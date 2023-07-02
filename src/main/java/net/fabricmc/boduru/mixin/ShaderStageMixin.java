package net.fabricmc.boduru.mixin;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.boduru.loading.FileFinder;
import net.minecraft.client.gl.GlImportProcessor;
import net.minecraft.client.gl.ShaderStage;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static net.fabricmc.boduru.main.WaterShaderMod.LOGGER;

@Mixin(ShaderStage.class)
public class ShaderStageMixin {
    /**
     * @author Boduru
     * @reason Load custom shaders
     * This mixin is responsible for loading custom shaders.
     * When Minecraft tries to load its shader file, we replace it with our own.
     * This way, we can make Minecraft load our custom shaders from our shaderpack,
     * that we have full control over.
     * Vertex and Fragment Shaders are loaded through this method.
     * JSON files are overlooked and not taken into account.
     * For this method to work, you need to have in your asset folder the same shader folder structure as Minecraft.
     */
    @Overwrite
    public static int load(ShaderStage.Type type, String name, InputStream stream, String domain, GlImportProcessor loader) throws IOException {
        String string = IOUtils.toString(stream, StandardCharsets.UTF_8);

        String filepath = FileFinder.WhereFileExists(name + type.getFileExtension());
        if (filepath != null) {
            string = FileFinder.ReadFile(filepath);
            LOGGER.info("Loaded custom shader: " + name + type.getFileExtension());
        }

        if (string == null) {
            throw new IOException("Could not load program " + type.getName());
        } else {
            int typ = (type.getFileExtension().equals(".vsh")) ? 35633 : 35632;
            int i = GlStateManager.glCreateShader(typ);
            GlStateManager.glShaderSource(i, loader.readSource(string));
            GlStateManager.glCompileShader(i);
            if (GlStateManager.glGetShaderi(i, GlConst.GL_COMPILE_STATUS) == 0) {
                String string2 = StringUtils.trim(GlStateManager.glGetShaderInfoLog(i, 32768));
                throw new IOException("Couldn't compile " + type.getName() + " program (" + domain + ", " + name + ") : " + string2);
            } else {
                return i;
            }
        }
    }
}