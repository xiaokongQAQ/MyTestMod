package com.example;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyTestMod implements ModInitializer {
	public static final String MOD_ID = "mytestmod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ModItems.initialize();
        Registry.register(Registries.ITEM, Identifier.of((MyTestMod.MOD_ID), "lightning_stick"),
				ModItems.LIGHTNING_STICK);
		Registry.register(Registries.ITEM, Identifier.of((MyTestMod.MOD_ID),
				"crimson_burst_fruit"),ModItems.CRIMSON_BURST_FRUIT);
		Registry.register(Registries.PARTICLE_TYPE,Identifier.of(MyTestMod.MOD_ID,"crimson_burst_particle"),
				ModItems.CRIMSON_BURST_PARTICLE);
	}
}