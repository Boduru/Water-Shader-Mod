package net.fabricmc.boduru.mixin;

import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(ShaderProgram.class)
public interface ShaderProgramMixin {
    @Accessor("samplers")
    Map<String, Object> getSamplers();

    @Accessor("samplerNames")
    List<String> getSamplerNames();

    @Accessor("loadedSamplerIds")
    List<Integer> getLoadedSamplerIds();

    @Accessor("uniforms")
    List<GlUniform> getUniforms();

    @Accessor("loadedUniformIds")
    List<Integer> getLoadedUniformIds();

    @Accessor("loadedUniforms")
    Map<String, GlUniform> getLoadedUniforms();
}
