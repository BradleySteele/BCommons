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

package me.bradleysteele.commons.util;

import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Bradley Steele
 */
public final class OfflinePlayers {

    private static final JsonParser parser = new JsonParser();
    private static final CloseableHttpClient client = HttpClientBuilder.create().build();

    private static final String ENDPOINT_SESSIONSERVER = "https://sessionserver.mojang.com/session/minecraft/profile/%s";
    private static final String ENDPOINT_PROFILES_NAMES = "https://api.mojang.com/user/profiles/%s/names";
    private static final String ENDPOINT_PROFILES = "https://api.mojang.com/users/profiles/minecraft/%s";

    private static final LoadingCache<String, UUID> uuidCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .build(new CacheLoader<String, UUID>() {

                @Override
                public UUID load(String name) throws Exception {
                    JsonObject object = getResponse(String.format(ENDPOINT_PROFILES, name)).getAsJsonObject();
                    return UUID.fromString(object.get("id").getAsString().replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})","$1-$2-$3-$4-$5"));
                }
            });

    private static final LoadingCache<UUID, String> nameCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, String>() {

                   @Override
                   public String load(UUID uuid) throws Exception {
                       // Dashes have to be removed from the UUID when passing it
                       // through Mojang API URLs.
                       JsonArray array = getResponse(String.format(ENDPOINT_PROFILES_NAMES, uuid.toString().replace("-", ""))).getAsJsonArray();

                       // Get the last object (current name) in the array.
                       JsonObject object = parser.parse(array.get(array.size() - 1).toString()).getAsJsonObject();
                       return object.get("name").getAsString();
                   }
               });

    private static final LoadingCache<UUID, String> skinCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, String>() {

                @Override
                public String load(UUID uuid) throws Exception {
                    JsonObject object = getResponse(String.format(ENDPOINT_SESSIONSERVER, uuid.toString().replace("-", ""))).getAsJsonObject();
                    JsonArray array = object.get("properties").getAsJsonArray();
                    JsonObject properties = array.get(array.size() - 1).getAsJsonObject();

                    // texture
                    JsonObject value = parser.parse(new String(Base64.getMimeDecoder().decode(properties.get("value").toString()))).getAsJsonObject();
                    JsonObject textures = value.get("textures").getAsJsonObject();
                    JsonObject skin = textures.get("SKIN").getAsJsonObject();

                    return skin.get("url").getAsString();
                }
            });

    public static UUID getUUID(String name) {
        try {
            return uuidCache.get(name);
        } catch (Exception e) {
            // Ignored
        }

        return null;
    }

    /**
     * Attempts to retrieve the latest name associated with the
     * provided unique id. Results are cached for 10 minutes.
     * <p>
     * Fallback will be returned if {@code nameCache#get} fails to
     * execute (api down, invalid uuid, etc).
     *
     * @param uuid     player's unique id.
     * @param fallback fallback name to return if retrieving failed.
     * @return the players name or the fallback if it could not be
     *         retrieved.
     */
    public static String getName(UUID uuid, String fallback) {
        String name = Players.getOfflinePlayer(uuid).getName();

        // If the player has never joined the server, they won't have
        // a userdata file, and null returned.
        if (name != null) {
            return name;
        }

        try {
            return nameCache.get(uuid);
        } catch (Exception e) {
            // Ignored
        }

        return fallback;
    }

    public static String getName(UUID uuid) {
        return getName(uuid, null);
    }

    public static String getSkinURL(UUID uuid, boolean refresh) {
        if (refresh) {
            skinCache.refresh(uuid);
        }

        try {
            return skinCache.get(uuid);
        } catch (Exception e) {
            // Ignored
        }

        return null;
    }

    public static String getSkinURL(UUID uuid) {
        return getSkinURL(uuid, false);
    }

    private static JsonElement getResponse(String url) throws IOException {
        HttpResponse response = client.execute(new HttpGet(url));
        return parser.parse(EntityUtils.toString(response.getEntity(), Charsets.UTF_8));
    }
}