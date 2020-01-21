/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package service.xtea;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.runelite.cache.IndexType;
import net.runelite.cache.fs.Archive;
import net.runelite.cache.fs.Index;
import net.runelite.cache.fs.Storage;
import net.runelite.cache.fs.Store;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.SpringBootWebApplication;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

@RestController
@RequestMapping("/xtea")
public class XteaController {
    public static HashMap<Integer, int[]> xteas = new HashMap<>();
    private Gson backupWriter = new GsonBuilder().setPrettyPrinting().create();
    private Store store;
	private Storage storage;
	private Index maps;
    private Archive archive;
    private byte[] archiveData;

    @RequestMapping("/get")
    public HashMap<Integer, int[]> get() {
        return xteas;
    }

    @RequestMapping("/submit")
    public void submitRegion(@RequestParam int region, int key1, int key2, int key3, int key4) {
        if (checkKeys(region, new int[]{key1, key2, key3, key4}))
            xteas.put(region, new int[]{key1, key2, key3, key4});
    }

    private boolean checkKeys(int regionId, int[] keys) {
        try {
            if (maps == null) {
            	try
				{
					store = SpringBootWebApplication.store;
					storage = store.getStorage();
					storage.load(store);
					maps = store.getIndex(IndexType.MAPS);
				}
            	catch(Exception e) {
					e.printStackTrace();
				}
            }

            int x = regionId >>> 8;
            int y = regionId & 0xFF;

            String archiveName = new StringBuilder()
                    .append('l')
                    .append(x)
                    .append('_')
                    .append(y)
                    .toString();
            archive = maps.findArchiveByName(archiveName);

            archiveData = storage.loadArchive(archive);

            try {
                maps.findArchiveByName(archiveName).decompress(archiveData, keys);
                return true;
            } catch (Exception e) {
                Logger.getAnonymousLogger().warning("Bad keys submitted for region: " + regionId);
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000)
    private void backupSounds()
    {
        try (FileWriter writer = new FileWriter(new File("./xteas.json")))
        {
            writer.write(backupWriter.toJson(xteas));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
