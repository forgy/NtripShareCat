package com.giserpeng.ntripshare.ntrip.Network;

import com.giserpeng.ntripshare.ntrip.Servers.NtripCaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NetworkProcessor implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(NetworkProcessor.class.getName());

    private Selector selector;
    private Thread thread;

    private static NetworkProcessor instance;
    private final Worker worker = Worker.getInstance();

    public static NetworkProcessor getInstance() {
        if (instance == null)
            instance = new NetworkProcessor();
        return instance;
    }

    private NetworkProcessor() {
        try {
            this.selector = Selector.open();
            this.thread = new Thread(this);
            this.thread.start();
        } catch (IOException e) {
            logger.info(" NetworkProcessor IOException e");
            logger.info(e.getMessage());
        }
    }

    /**
     * When the new caster has created, this method registered his server socket channel in the selector.
     *
     * @param socket
     * @param caster
     * @return SelectionKey
     * @throws IOException
     */
    public SelectionKey registerServerChannel(ServerSocketChannel socket, NtripCaster caster) throws IOException {
        SelectionKey key = socket.register(this.selector, SelectionKey.OP_ACCEPT, caster);
        selector.wakeup();
        return key;
    }

    public void close() throws IOException {
        this.thread.interrupt();
        this.selector.close();
    }

    public void run() {
        while (true) {
            try {
                int count = selector.select();
                if (count < 1)
                    continue;

                System.out.println(1);
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();

                    if (!selectionKey.isValid()) {
                        logger.debug("SelectionKey is not valid!");
                        continue;
                    }

                    if (selectionKey.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
                        SocketChannel connectSocket = server.accept();
                        connectSocket.configureBlocking(false);
                        SelectionKey clientKey = connectSocket.register(this.selector, SelectionKey.OP_READ);
                        new ConnectHandler(clientKey, (NtripCaster) selectionKey.attachment());
                        logger.debug("Socket accept.");

                    } else if (selectionKey.isReadable()) {
                        IWork work = (IWork) selectionKey.attachment();
                        try {
                            work.readSelf();
                            worker.addWork(work);
                        } catch (IOException | InterruptedException e) {
                            logger.info(" NetworkProcessor IOException e");
                            logger.error(e.getMessage());
                            work.close();
                            selectionKey.cancel();
                        }
                    }
                    iterator.remove();
                }
            } catch (Exception ex) {
//                logger.error( ex.getMessage());
            }
        }
    }
}
