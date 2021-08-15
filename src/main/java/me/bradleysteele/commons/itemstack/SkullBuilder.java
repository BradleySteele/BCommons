/*
 * Copyright 2018 Bradley Steele
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.bradleysteele.commons.itemstack;

import me.bradleysteele.commons.nms.wrapped.profile.NMSGameProfile;
import me.bradleysteele.commons.nms.wrapped.profile.NMSProperty;
import me.bradleysteele.commons.util.Players;
import me.bradleysteele.commons.util.reflect.Reflection;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Base64;
import java.util.UUID;

/**
 * @author Bradley Steele
 */
public class SkullBuilder extends ItemStackBuilder {

    private static final String TEXTURES_JSON = "{ textures: { SKIN: { url: \"%s\" } } }";

    private String owner;
    private String url;

    protected SkullBuilder(String owner) {
        super(new ItemStack(ItemStacks.PLAYER_HEAD, 1, (short) 3));

        this.owner = owner;
    }

    protected SkullBuilder(ItemStack stack) {
        super(stack);
    }

    protected SkullBuilder(SkullBuilder builder) {
        super(builder);

        owner = builder.owner;
        url = builder.url;
    }

    protected SkullBuilder() {
        this((String) null);
    }

    @Override
    public ItemStack build() {
        ItemStack stack = super.build();
        SkullMeta meta = (SkullMeta) stack.getItemMeta();

        if (owner != null) {
            meta.setOwner(owner);
        }

        if (url != null) {
            NMSGameProfile profile = new NMSGameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new NMSProperty("textures", Base64.getMimeEncoder().encodeToString(String.format(TEXTURES_JSON, url).getBytes())));

            // Apply to meta
            Reflection.setFieldValue("profile", meta, profile.getNMSHandle());
        }

        stack.setItemMeta(meta);

        // NBTs must be applied AFTER meta is applied.
        for (Applier applier : this.getNBTAppliers()) {
            stack = applier.apply(stack);
        }

        return stack;
    }

    /**
     * @param owner skull owner (player name).
     * @return this skull builder.
     */
    public SkullBuilder withOwner(String owner) {
        this.owner = owner;
        return this;
    }

    /**
     * @param player skull owner.
     * @return this skull builder.
     */
    public SkullBuilder withOwner(OfflinePlayer player) {
        this.owner = player.getName();
        return this;
    }

    /**
     * @param uuid skull owner's unique id.
     * @return this skull builder.
     */
    public SkullBuilder withOwner(UUID uuid) {
        this.owner = Players.getOfflinePlayer(uuid).getName();
        return this;
    }

    /**
     * @param url skin url.
     * @return this skull builder.
     */
    public SkullBuilder withURL(String url) {
        this.url = url;
        return this;
    }
}