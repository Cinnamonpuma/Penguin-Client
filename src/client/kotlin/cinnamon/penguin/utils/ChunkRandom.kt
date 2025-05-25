package cinnamon.penguin.utils

import net.minecraft.util.math.random.ChunkRandom as VanillaChunkRandom
import net.minecraft.util.math.random.Random // Vanilla Random
import net.minecraft.world.gen.random.XoroshiroRandom // Vanilla XoroshiroRandom

// Wrapper class for vanilla ChunkRandom to ensure accuracy
class PenguinChunkRandom {
    private val vanillaChunkRandom: VanillaChunkRandom

    // Constructor that mimics `new ChunkRandom(ChunkRandom.RandomProvider.XOROSHIRO.create(seed))`
    constructor(seed: Long) {
        // In modern MC, XoroshiroRandom itself is a Random. Provider might be deprecated.
        // ChunkRandom constructor takes a Random instance.
        this.vanillaChunkRandom = VanillaChunkRandom(XoroshiroRandom(seed))
    }
    
    // Alternative constructor if a direct Random instance is preferred by caller
    constructor(random: Random) {
        this.vanillaChunkRandom = VanillaChunkRandom(random)
    }

    fun setPopulationSeed(worldSeed: Long, chunkBlockX: Int, chunkBlockZ: Int): Long {
        return vanillaChunkRandom.setPopulationSeed(worldSeed, chunkBlockX, chunkBlockZ)
    }

    fun setDecoratorSeed(populationSeed: Long, featureIndex: Int, featureStep: Int) {
        vanillaChunkRandom.setDecoratorSeed(populationSeed, featureIndex, featureStep)
    }

    fun nextInt(bound: Int): Int {
        return vanillaChunkRandom.nextInt(bound)
    }
    
    fun nextInt(): Int {
        return vanillaChunkRandom.nextInt()
    }

    fun nextFloat(): Float {
        return vanillaChunkRandom.nextFloat()
    }

    fun nextDouble(): Double {
        return vanillaChunkRandom.nextDouble()
    }
}
