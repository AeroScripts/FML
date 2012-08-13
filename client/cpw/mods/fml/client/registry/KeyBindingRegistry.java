package cpw.mods.fml.client.registry;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;

import net.minecraft.src.GameSettings;
import net.minecraft.src.KeyBinding;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.registry.TickRegistry;

public class KeyBindingRegistry
{
    /**
     * Register a KeyHandler to the game. This handler will be called on certain tick events
     * if any of its key is inactive or has recently changed state
     *
     * @param handler
     */
    public static void registerKeyBinding(KeyHandler handler) {
        instance().keyHandlers.add(handler);
        TickRegistry.registerTickHandler(handler, Side.CLIENT);
    }


    /**
     * Extend this class to register a KeyBinding and recieve callback
     * when the key binding is triggered
     *
     * @author cpw
     *
     */
    public static abstract class KeyHandler implements ITickHandler
    {
        protected KeyBinding keyBinding;
        protected boolean keyDown;
        protected boolean repeating;

        public KeyHandler(KeyBinding keyBinding, boolean repeating)
        {
            this.keyBinding = keyBinding;
            this.repeating = repeating;
        }

        public KeyBinding getKeyBinding()
        {
            return this.keyBinding;
        }

        /**
         * Not to be overridden - KeyBindings are tickhandlers under the covers
         */
        @Override
        public final void tickStart(EnumSet<TickType> type, Object... tickData)
        {
            keyTick(type, false);
        }

        /**
         * Not to be overridden - KeyBindings are tickhandlers under the covers
         */
        @Override
        public final void tickEnd(EnumSet<TickType> type, Object... tickData)
        {
            keyTick(type, true);
        }

        private void keyTick(EnumSet<TickType> type, boolean tickEnd)
        {
            int keyCode = keyBinding.field_74512_d;
            boolean state = (keyCode < 0 ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode));
            if (state != keyDown || (state && repeating))
            {
                if (state)
                {
                    keyDown(type, tickEnd, state!=keyDown);
                }
                else
                {
                    keyUp(type, tickEnd);
                }
                keyDown = state;
            }
        }

        /**
         * Called when the key is first in the down position on any tick from the {@link #ticks()}
         * set. Will be called subsequently with isRepeat set to true
         *
         * @see #keyUp(EnumSet, boolean)
         *
         * @param types the type(s) of tick that fired when this key was first down
         * @param tickEnd was it an end or start tick which fired the key
         * @param isRepeat is it a repeat key event
         */
        public abstract void keyDown(EnumSet<TickType> types, boolean tickEnd, boolean isRepeat);
        /**
         * Fired once when the key changes state from down to up
         *
         * @see #keyDown(EnumSet, boolean, boolean)
         *
         * @param types the type(s) of tick that fired when this key was first down
         * @param tickEnd was it an end or start tick which fired the key
         */
        public abstract void keyUp(EnumSet<TickType> types, boolean tickEnd);


        /**
         * This is the list of ticks for which the key binding should trigger. The only
         * valid ticks are client side ticks, obviously.
         *
         * @see cpw.mods.fml.common.ITickHandler#ticks()
         */
        public abstract EnumSet<TickType> ticks();
    }

    private static final KeyBindingRegistry INSTANCE = new KeyBindingRegistry();

    private List<KeyHandler> keyHandlers = Lists.newArrayList();

    @Deprecated
    public static KeyBindingRegistry instance()
    {
        return INSTANCE;
    }


    public void uploadKeyBindingsToGame(GameSettings settings)
    {
        ArrayList<KeyBinding> harvestedBindings = Lists.newArrayList();
        for (KeyHandler key : keyHandlers)
        {
            harvestedBindings.add(key.keyBinding);
        }
        KeyBinding[] modKeyBindings = harvestedBindings.toArray(new KeyBinding[harvestedBindings.size()]);
        KeyBinding[] allKeys = new KeyBinding[settings.field_74324_K.length + modKeyBindings.length];
        System.arraycopy(settings.field_74324_K, 0, allKeys, 0, settings.field_74324_K.length);
        System.arraycopy(modKeyBindings, 0, allKeys, settings.field_74324_K.length, modKeyBindings.length);
        settings.field_74324_K = allKeys;
        settings.func_74300_a();
    }
}
