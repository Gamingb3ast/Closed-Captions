package ninja.genuine.caption.events;

import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.sound.PlaySoundEvent17;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import ninja.genuine.caption.ClosedCaption;
import ninja.genuine.caption.caption.CaptionHUD;
import ninja.genuine.caption.caption.CaptionWorld;
import ninja.genuine.caption.container.ContainerHUD;
import ninja.genuine.caption.container.ContainerWorld;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class SoundEvents {

	private static boolean displayBoth(final String name) {
		return name.contains("primed");
	}

	private static boolean isCaptionHUD(final CaptionWorld caption) {
		return SoundEvents.isEntityHUD(caption) || SoundEvents.isEntityPlayer(caption);
	}

	private static boolean isEntityHUD(final CaptionWorld caption) {
		return caption.entity instanceof EntityItem || caption.entity instanceof EntityXPOrb || caption.sound instanceof PositionedSoundRecord;
	}

	private static boolean isEntityPlayer(final CaptionWorld caption) {
		final EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
		return player != null && caption.entity instanceof EntityPlayer && player.getDisplayName().equals(((EntityPlayer) caption.entity).getDisplayName());
	}

	private static boolean isNameBroken(final String name) {
		return name == null || name.isEmpty() || name.equalsIgnoreCase("none") || name.endsWith(":none");
	}

	private static boolean isRemote(final PlaySoundAtEntityEvent event) {
		return event == null || event.entity == null || event.entity.worldObj == null || event.entity.worldObj.isRemote;
	}

	public final ContainerHUD hud = new ContainerHUD();
	public final ContainerWorld world = new ContainerWorld();

	public void createCaption(final String name, final Entity entity, final float volume, final float pitch) {
		final CaptionWorld caption3D = new CaptionWorld(name, entity, volume, pitch);
		final CaptionHUD caption2D = new CaptionHUD(name, volume, pitch);
		if (SoundEvents.displayBoth(name)) {
			hud.message(caption2D);
			world.add(caption3D);
		} else if (SoundEvents.isCaptionHUD(caption3D))
			hud.message(caption2D);
		else
			world.add(caption3D);
	}

	public void createCaption(final String name, final ISound sound) {
		final CaptionWorld caption3D = new CaptionWorld(name, sound, sound.getVolume(), sound.getPitch());
		final CaptionHUD caption2D = new CaptionHUD(name, sound.getVolume(), sound.getPitch());
		if (SoundEvents.displayBoth(name)) {
			hud.message(caption2D);
			world.add(caption3D);
		} else if (SoundEvents.isCaptionHUD(caption3D))
			hud.message(caption2D);
		else
			world.add(caption3D);
	}

	@SubscribeEvent
	public void eventEntity(final PlaySoundAtEntityEvent event) {
		if (SoundEvents.isRemote(event) || SoundEvents.isNameBroken(event.name) || !ClosedCaption.enabled)
			return;
		createCaption(event.name, event.entity, event.volume, event.pitch);
	}

	@SubscribeEvent
	public void eventISound(final PlaySoundEvent17 event) {
		if (event.category == null || event.sound == null || SoundEvents.isNameBroken(event.name) || !ClosedCaption.enabled)
			return;
		switch (event.category) {
			case PLAYERS:
			case ANIMALS:
			case MOBS:
				return;
			default:
				createCaption(event.name, event.sound);
				break;
		}
	}

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Minecraft.getMinecraft().inGameHasFocus) {
            if (ClosedCaption.toggleKey.isPressed()) {
                ClosedCaption.enabled = !ClosedCaption.enabled;
            }
        }
    }

	public void registerEvents() {
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(hud);
		MinecraftForge.EVENT_BUS.register(world);
		FMLCommonHandler.instance().bus().register(hud);
		FMLCommonHandler.instance().bus().register(world);
        FMLCommonHandler.instance().bus().register(this);
	}
}
