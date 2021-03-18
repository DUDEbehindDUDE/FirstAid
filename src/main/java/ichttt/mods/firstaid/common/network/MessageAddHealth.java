/*
 * FirstAid
 * Copyright (C) 2017-2021
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ichttt.mods.firstaid.common.network;

import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageAddHealth {
    private final float[] table;

    public MessageAddHealth(PacketBuffer buffer) {
        this.table = new float[8];
        for (int i = 0; i < 8; i++) {
            this.table[i] = buffer.readFloat();
        }
    }

    public MessageAddHealth(float[] table) {
        this.table = table;
    }

    public void encode(PacketBuffer buf) {
        for (float f : table)
            buf.writeFloat(f);
    }

    public static class Handler {

        public static void onMessage(MessageAddHealth message, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context ctx = supplier.get();
            CommonUtils.checkClient(ctx);
            ctx.enqueueWork(() -> {
                ClientPlayerEntity playerSP = Minecraft.getInstance().player;
                AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(playerSP);
                for (int i = 0; i < message.table.length; i++) {
                    float f = message.table[i];
                    EnumPlayerPart part = EnumPlayerPart.VALUES[i];
                    damageModel.getFromEnum(part).heal(f, playerSP, false);
                }
            });
        }
    }
}
