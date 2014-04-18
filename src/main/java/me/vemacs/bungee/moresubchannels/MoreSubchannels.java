package me.vemacs.bungee.moresubchannels;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.*;

public class MoreSubchannels extends Plugin implements Listener {
    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, this);
    }

    private static void sendMessageString(String message, ServerInfo server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(message);
        server.sendData("BungeeCord", out.toByteArray());
    }


    private static void sendMessageInt(int message, ServerInfo server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeInt(message);
        server.sendData("BungeeCord", out.toByteArray());
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (event.getTag().equals("BungeeCord") && event.getSender() instanceof Server) {
            ByteArrayInputStream stream = new ByteArrayInputStream(event.getData());
            DataInputStream in = new DataInputStream(stream);
            final ServerInfo sender = ((Server) event.getSender()).getInfo();
            try {
                String subChannel = in.readUTF();
                switch (subChannel) {
                    case "ServerIP": {
                        String server = in.readUTF();
                        ServerInfo info = getProxy().getServerInfo(server);
                        sendMessageString(info.getAddress().getAddress().getHostAddress() + ":" +
                                info.getAddress().getPort(), sender);
                        break;
                    }
                    case "MOTD": {
                        String server = in.readUTF();
                        ServerInfo info = getProxy().getServerInfo(server);
                        info.ping(new Callback<ServerPing>() {
                            @Override
                            public void done(ServerPing serverPing, Throwable throwable) {
                                sendMessageString(serverPing == null ? "" : serverPing.getDescription(), sender);
                            }
                        });
                        break;
                    }
                    case "MaxPlayers": {
                        String server = in.readUTF();
                        ServerInfo info = getProxy().getServerInfo(server);
                        info.ping(new Callback<ServerPing>() {
                            @Override
                            public void done(ServerPing serverPing, Throwable throwable) {
                                sendMessageInt(serverPing == null ? -1 : serverPing.getPlayers().getMax(), sender);
                            }
                        });
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}