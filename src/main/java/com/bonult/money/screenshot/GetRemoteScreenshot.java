package com.bonult.money.screenshot;

import com.bonult.money.Main;
import com.bonult.money.MakingMoneyWithQA;
import com.bonult.money.ConfigHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 通过安卓APP获取手机截图(任务由远程启动)
 *
 * @author bonult
 */
public class GetRemoteScreenshot implements GetScreenshot {

	private static final Logger LOGGER = LoggerFactory.getLogger(GetRemoteScreenshot.class);

	private static byte[] buffer;
	private Server server;

	public GetRemoteScreenshot() throws Exception{
		server = new Server();
		new Thread(server).start();
	}

	public byte[] getImg(){
		return buffer;
	}

	private static class Server implements Runnable {

		private int port = ConfigHolder.CONFIG.getPort();
		private ServerSocket serverSocket;
//		private final ExecutorService threadPool = Executors.newFixedThreadPool(3);;

		private Server() throws Exception{
			serverSocket = new ServerSocket(port, 3);
			Main.infoMsgShow("服务器已启动!","");
		}

		public void run(){
			while(true){
				Socket socket = null;
				if(serverSocket.isClosed())
					break;
				try{
					socket = serverSocket.accept();

					InputStream in = socket.getInputStream();

					ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
					byte[] buff = new byte[4096];
					int rc;
					while((rc = in.read(buff, 0, 4096)) > 0){
						swapStream.write(buff, 0, rc);
					}
					buffer = swapStream.toByteArray();

					OutputStream out = socket.getOutputStream();
					if(buffer.length == 1){
						out.write((ConfigHolder.CONFIG.getProblemAreaX() + "").getBytes());
						out.write("|".getBytes());
						out.write((ConfigHolder.CONFIG.getProblemAreaY() + "").getBytes());
						out.write("|".getBytes());
						out.write((ConfigHolder.CONFIG.getProblemAreaWidth() + "").getBytes());
						out.write("|".getBytes());
						out.write((ConfigHolder.CONFIG.getProblemAreaHeight() + "").getBytes());
						out.flush();
						socket.shutdownOutput();
					}
				}catch(IOException e){
					LOGGER.error(e.getMessage(), e);
					buffer = null;
				}finally{
					if(socket != null){
						try{
							socket.close();
						}catch(IOException e){
							LOGGER.error(e.getMessage(), e);
						}
					}
				}
				MakingMoneyWithQA.instance.run();
				try{
					File file = new File(ConfigHolder.CONFIG.getImageTempPath() + "phone.png");
					OutputStream o = new FileOutputStream(file);
					o.write(buffer);
					o.close();
				}catch(Exception e){
				}

			}
		}

		public void close() throws IOException{
//			threadPool.shutdown();
			serverSocket.close();
		}
	}

	@Override
	public void close(){
		if(server != null){
			try{
				server.close();
			}catch(IOException e){
				Main.errorMsgShow(e.getMessage(),"关闭网络连接出错");
			}
		}
	}
}
