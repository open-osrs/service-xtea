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

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import net.runelite.cache.IndexType;
import net.runelite.cache.fs.*;
import org.springframework.web.bind.annotation.*;
import service.SpringBootWebApplication;

@RestController
@RequestMapping("/xtea")
public class XteaController
{
	private HashMap<Integer, int[]> xteas = new HashMap<>();
	private Store store;

	@RequestMapping("/get")
	public HashMap<Integer, int[]> get()
	{
		return xteas;
	}

	@RequestMapping("/submit")
	public void submitRegion(@RequestParam int region, int key1, int key2, int key3, int key4)
	{
		if (checkKeys(region, new int[]{key1,key2,key3,key4}))
		xteas.put(region, new int[]{key1,key2,key3,key4});
	}

	private boolean checkKeys(int regionId, int[] keys)
	{
		try {
			store = SpringBootWebApplication.store;
			Storage storage = store.getStorage();
			storage.load(store);
			Index maps = store.getIndex(IndexType.MAPS);

			int x = regionId >>> 8;
			int y = regionId & 0xFF;

			String archiveName = new StringBuilder()
					.append('l')
					.append(x)
					.append('_')
					.append(y)
					.toString();
			Archive archive = maps.findArchiveByName(archiveName);

			byte[] archiveData = storage.loadArchive(archive);

			try
			{
				maps.findArchiveByName(archiveName).decompress(archiveData, keys);
				return true;
			}
			catch (Exception e)
			{
				Logger.getAnonymousLogger().warning("Bad keys submitted for region: " + regionId);
				return false;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
