package us.teaminceptus.novaconomy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import us.teaminceptus.novaconomy.abstraction.Wrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public final class Wrapper1_12_R1 implements Wrapper {

    @Override
    public void sendActionbar(Player p, String message) {
        sendActionbar(p, new TextComponent(message));
    }

    @Override
    public void sendActionbar(Player p, BaseComponent component) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
    }

    @Override
    public String getNBTString(org.bukkit.inventory.ItemStack item, String key) {
        ItemStack nmsitem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nmsitem.hasTag() ? nmsitem.getTag() : new NBTTagCompound();
        NBTTagCompound novaconomy = tag.getCompound(ROOT);

        return novaconomy.getString(key);
    }

    @Override
    public void setNBT(org.bukkit.inventory.ItemStack item, String key, String value) {
        ItemStack nmsitem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nmsitem.hasTag() ? nmsitem.getTag() : new NBTTagCompound();
        NBTTagCompound novaconomy = tag.getCompound(ROOT);

        novaconomy.setString(key, value);
        tag.set(ROOT, novaconomy);
        nmsitem.setTag(tag);
    }

    @Override
    public void openBook(Player p, org.bukkit.inventory.ItemStack book) {
        int slot = p.getInventory().getHeldItemSlot();
        org.bukkit.inventory.ItemStack old = p.getInventory().getItem(slot);
        p.getInventory().setItem(slot, book);

        ByteBuf buf = Unpooled.buffer(256);
        buf.setByte(0, 0);
        buf.writerIndex(1);

        PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload("MC|BOpen", new PacketDataSerializer(buf));
        PlayerConnection pc = ((CraftPlayer) p).getHandle().playerConnection;
        pc.sendPacket(packet);
        p.getInventory().setItem(slot, old);
    }

    @Override
    public org.bukkit.inventory.ItemStack getGUIBackground() {
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(Material.STAINED_GLASS_PANE, 1, (short)15);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public org.bukkit.inventory.ItemStack createSkull(OfflinePlayer p) {
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner(p.getName());

        return item;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public <T extends ConfigurationSerializable> T getNBTSerializable(org.bukkit.inventory.ItemStack item, String key, Class<T> clazz) {
        try {
            ItemStack nmsitem = CraftItemStack.asNMSCopy(item);
            NBTTagCompound tag = nmsitem.hasTag() ? nmsitem.getTag() : new NBTTagCompound();
            NBTTagCompound novaconomy = tag.getCompound(ROOT);

            Map map = (Map) getData(novaconomy);

            Method m = clazz.getDeclaredMethod("deserialize", Map.class);
            return clazz.cast(m.invoke(null, map));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object getData(NBTBase b) {
        switch (b.getTypeId()) {
            case 1: return ((NBTTagByte) b).f();
            case 2: return ((NBTTagShort) b).e();
            case 3: return ((NBTTagInt) b).d();
            case 4: return ((NBTTagLong) b).c();
            case 5: return ((NBTTagFloat) b).i();
            case 6: return ((NBTTagDouble) b).g();
            case 7: return ((NBTTagByteArray) b).c();
            case 8: return ((NBTTagString) b).c_();
            case 9: {
                List l = new ArrayList<>();

                NBTTagList list = (NBTTagList) b;
                for (int i = 0; i < list.size(); i++) l.add(getData(list.i(i)));
                return l;
            }
            case 10: {
                NBTTagCompound c = (NBTTagCompound) b;
                Map<String, Object> map = new HashMap<>();

                c.c().forEach(s -> map.put(s, getData(c.get(s))));
                return map;
            }
            case 11: return ((NBTTagIntArray) b).d();
            case 12: {
                try {
                    Field f = NBTTagLongArray.class.getDeclaredField("b");
                    f.setAccessible(true);

                    return f.get(b);
                } catch (Exception e) {
                    e.printStackTrace();;
                    return null;
                }
            }

            default: return null;
        }
    }

    @Override
    public void setNBT(org.bukkit.inventory.ItemStack item, String key, ConfigurationSerializable serializable) {
        try {
            ItemStack nmsitem = CraftItemStack.asNMSCopy(item);
            NBTTagCompound tag = nmsitem.hasTag() ? nmsitem.getTag() : new NBTTagCompound();
            NBTTagCompound novaconomy = tag.getCompound(ROOT);

            NBTTagCompound cmp = MojangsonParser.parse(serializable.serialize().toString());
            novaconomy.set(key, cmp);
            tag.set(ROOT, novaconomy);
            nmsitem.setTag(tag);
        } catch (MojangsonParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setNBT(org.bukkit.inventory.ItemStack item, String key, org.bukkit.inventory.ItemStack value) {
        ItemStack nmsitem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nmsitem.hasTag() ? nmsitem.getTag() : new NBTTagCompound();
        NBTTagCompound novaconomy = tag.getCompound(ROOT);

        novaconomy.set(key, CraftItemStack.asNMSCopy(value).getTag());
        tag.set(ROOT, novaconomy);
        nmsitem.setTag(tag);
    }

    @Override
    public org.bukkit.inventory.ItemStack getNBTItem(org.bukkit.inventory.ItemStack item, String key) {
        ItemStack nmsitem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nmsitem.hasTag() ? nmsitem.getTag() : new NBTTagCompound();
        NBTTagCompound novaconomy = tag.getCompound(ROOT);

        NBTTagCompound nbt = novaconomy.getCompound(key);
        return CraftItemStack.asBukkitCopy(new ItemStack(nbt));
    }

    @Override
    public double getNBTDouble(org.bukkit.inventory.ItemStack item, String key) {
        ItemStack nmsitem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nmsitem.hasTag() ? nmsitem.getTag() : new NBTTagCompound();
        NBTTagCompound novaconomy = tag.getCompound(ROOT);

        return novaconomy.getDouble(key);
    }

    @Override
    public void setNBT(org.bukkit.inventory.ItemStack item, String key, double value) {
        ItemStack nmsitem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nmsitem.hasTag() ? nmsitem.getTag() : new NBTTagCompound();
        NBTTagCompound novaconomy = tag.getCompound(ROOT);

        novaconomy.setDouble(key, value);
        tag.set(ROOT, novaconomy);
        nmsitem.setTag(tag);
    }

}
