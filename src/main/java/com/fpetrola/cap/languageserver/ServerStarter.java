package com.fpetrola.cap.languageserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

public class ServerStarter {

	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
//		startServer(System.in, System.out);
		main2();
	}

	public static void startServer(InputStream in, OutputStream out) throws InterruptedException, ExecutionException, IOException {
		CapLanguageServer server = new CapLanguageServer();
		Launcher<LanguageClient> l = LSPLauncher.createServerLauncher(server, in, out);
		Future<?> startListening = l.startListening();
		server.setRemoteProxy(l.getRemoteProxy());
		startListening.get();
	}

	private static void main2() throws IOException, InterruptedException, ExecutionException {
		CapLanguageServer languageServer = new CapLanguageServer();

		final ServerSocketChannel serverSocket = ServerSocketChannel.open();
		InetSocketAddress _inetSocketAddress = new InetSocketAddress("localhost", 5007);
		serverSocket.bind(_inetSocketAddress);
		final SocketChannel socketChannel = serverSocket.accept();
		InputStream _newInputStream = Channels.newInputStream(socketChannel);
		OutputStream _newOutputStream = Channels.newOutputStream(socketChannel);
		PrintWriter _printWriter = new PrintWriter(System.out);
		final Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(languageServer, _newInputStream, _newOutputStream, true, _printWriter);
		languageServer.setRemoteProxy(launcher.getRemoteProxy());
		launcher.startListening().get();
	}

}
