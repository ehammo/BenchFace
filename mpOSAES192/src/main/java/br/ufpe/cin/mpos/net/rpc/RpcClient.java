/*******************************************************************************
 * Copyright (C) 2014 Philipp B. Costa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.ufpe.cin.mpos.net.rpc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import br.ufc.mdcc.mpos.net.Protocol;
import br.ufc.mdcc.mpos.net.core.ClientTcp;
import br.ufc.mdcc.mpos.net.core.FactoryClient;
import br.ufc.mdcc.mpos.net.endpoint.ServerContent;
import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;
import br.ufc.mdcc.mpos.net.rpc.model.Code;
import br.ufc.mdcc.mpos.net.rpc.model.RpcProfile;
import br.ufc.mdcc.mpos.net.rpc.util.DebugBufferedInputStream;
import br.ufc.mdcc.mpos.net.rpc.util.RpcException;
import br.ufc.mdcc.mpos.net.rpc.util.RpcSerializable;
import br.ufpe.cin.mpos.criptografia.Cripto;

/**
 * This is a client implementation used for rpc call, under 
 * TCP protocol.
 * 
 * @author Philipp B. Costa
 */
public final class RpcClient {
	private final int BUFFER_SIZE = 4096;
	private final RpcProfile profile;
	private final byte dataFlag[] = new byte[1];

	private ServerContent server;

	private InputStream inputStream;
	private OutputStream outputStream;

	public RpcClient() {
		this.profile = new RpcProfile();
		this.server = null;
	}

	public void setupServer(ServerContent server) {
		this.server = server;
	}

	public Object call(Object objOriginal, Method method, Object params[]) throws RpcException, ConnectException, GeneralSecurityException {
		return call(false, objOriginal, method.getName(), false, params);
	}

	public Object call(boolean manualSerialization, Object objOriginal, String methodName, boolean cripto, Object params[]) throws RpcException, ConnectException, GeneralSecurityException {
		return call(false, manualSerialization, objOriginal, methodName, cripto, params);
	}

	public Object call(boolean needProfile, boolean manualSerialization, Object objOriginal, String methodName, boolean cripto, Object params[]) throws RpcException, ConnectException, GeneralSecurityException {
		if (server == null) {
			throw new ConnectException("Need to setup any server for use the RPC Client");
		}

		ClientTcp client = (ClientTcp) FactoryClient.getInstance(Protocol.TCP_STREAM);

		try {
			client.connect(server.getIp(), server.getRpcServicePort());

			sent(client.getOutputStream(), manualSerialization, needProfile, objOriginal, methodName, cripto, params);
			ResponseRemotable response = receive(client.getInputStream(), objOriginal, methodName);

			if (response.code == Code.OK) {
				close(client);// reduce server wait client finish!
				return response.methodReturn;
			} else if (response.code == Code.METHOD_THROW_ERROR) {
				close(client);
				throw new RpcException("[Server]: Remote method thrown some errors\n" + response.except);
			} else {
				close(client);
				throw new RpcException("[Server]: RPC call terminated with some errors!\n" + response.except);
			}
		} catch (ConnectException e) {
			close(client);
			throw new ConnectException("Failed to connect to server: " + server.getIp() + ":" + server.getRpcServicePort());
		} catch (IOException e) {
			close(client);
			throw new RpcException("RPC suffer with some I/O error, check yours dependences: " + objOriginal.getClass().getName());
		} catch (MissedEventException e) {
			close(client);
			throw new RpcException("ERROR: TCP thread was started?");
		} catch (ClassNotFoundException e) {
			close(client);
			throw new RpcException("ERROR: Deploy jar wasn't updated from assets!");
		}
	}

	private void sent(OutputStream os, boolean manualSerialization, boolean debug, Object objOriginal, String methodName, boolean cripto, Object params[]) throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, ClassNotFoundException {

		if (manualSerialization) {
			dataFlag[0] = debug ? Code.DATASTREAMDEBUG : Code.DATASTREAM;
			os.write(dataFlag);

			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(os, BUFFER_SIZE));
			outputStream = dos;

			dos.writeUTF(objOriginal.getClass().getName());
			dos.writeUTF(methodName);
			((RpcSerializable) objOriginal).writeMethodParams(dos, methodName, params);
			dos.flush();
		} else {
			dataFlag[0] = debug ? Code.OBJECTSTREAMDEBUG : Code.OBJECTSTREAM;
			os.write(dataFlag);

			ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(os, BUFFER_SIZE));
			outputStream = oos;

			oos.writeUTF(objOriginal.getClass().getName());
			oos.writeUTF(methodName);
			
			if (cripto) {
				Cripto AES = new Cripto();
				Object cifradoParams = AES.encripta(params);
				oos.writeBoolean(cripto);
				oos.writeObject(cifradoParams);
			} else {
					oos.writeBoolean(cripto);
					oos.writeObject(params);
			}
			
			oos.flush();
		}
	}

	private ResponseRemotable receive(InputStream is, Object objOriginal, String methodName) throws IOException, ClassNotFoundException, NoSuchPaddingException, GeneralSecurityException {
		ResponseRemotable response = new ResponseRemotable();

		is.read(dataFlag);
		if (dataFlag[0] == Code.DATASTREAM) {
			DataInputStream dis = new DataInputStream(new BufferedInputStream(is, BUFFER_SIZE));
			inputStream = dis;

			int code = dis.readInt();
			response.code = code;
			if (code == Code.OK) {
				response.methodReturn = ((RpcSerializable) objOriginal).readMethodReturn(dis, methodName);
			} else if (code == Code.METHOD_THROW_ERROR) {
				response.except = dis.readUTF();
			} else {
				throw new IOException("Code different from expected: " + code);
			}
		} else if (dataFlag[0] == Code.OBJECTSTREAM) {
			ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(is, BUFFER_SIZE));
			inputStream = ois;

			response.code = ois.readInt();

            response.cripto = ois.readBoolean();
        	if (response.cripto) {            
        		Object cifradoMethodParams = ois.readObject();
        		Cripto AES = new Cripto();
        		response.methodReturn = AES.decripta((byte[]) cifradoMethodParams);
			} else {
				response.methodReturn = ois.readObject();
			}
			
		} else if (dataFlag[0] == Code.DATASTREAMDEBUG) {
			DebugBufferedInputStream bufInput = new DebugBufferedInputStream(is, BUFFER_SIZE);
			DataInputStream dis = new DataInputStream(bufInput);
			inputStream = dis;

			int code = dis.readInt();
			response.code = code;
			if (code == Code.OK) {
				profile.setUploadSize(dis.readInt());
				profile.setUploadTime(dis.readLong());
				profile.setExecutionCpuTime(dis.readLong());
				long initDownloadTime = System.currentTimeMillis();
				response.methodReturn = ((RpcSerializable) objOriginal).readMethodReturn(dis, methodName);
				profile.setDonwloadTime(System.currentTimeMillis() - initDownloadTime);
				profile.setDownloadSize(bufInput.getTotalReadData());
			} else if (code == Code.METHOD_THROW_ERROR) {
				response.except = dis.readUTF();
			} else {
				response.except = "Code different from expected: " + code;
			}
		} else if (dataFlag[0] == Code.OBJECTSTREAMDEBUG) { // RECEBIMENTO POR AQUI!!!
			DebugBufferedInputStream bufInput = new DebugBufferedInputStream(is, BUFFER_SIZE);
			ObjectInputStream ois = new ObjectInputStream(bufInput);
			
			inputStream = ois;

			response.code = ois.readInt();
			profile.setUploadSize(ois.readInt());
			profile.setUploadTime(ois.readLong());
			profile.setExecutionCpuTime(ois.readLong());

			long initDownloadTime = System.currentTimeMillis();
			
            response.cripto = ois.readBoolean();
        	if (response.cripto) {            
        		Object cifradoMethodParams = ois.readObject();
        		Cripto AES = new Cripto();
        		response.methodReturn = AES.decripta((byte[]) cifradoMethodParams);
			} else {
				response.methodReturn = ois.readObject();
			}
			
			profile.setDonwloadTime(System.currentTimeMillis() - initDownloadTime);
			profile.setDownloadSize(bufInput.getTotalReadData());
		}

		return response;
	}

	private void close(ClientTcp client) {
		try {
			outputStream.close();
			inputStream.close();
			client.close();
		} catch (IOException e) {
		}
	}

	public RpcProfile getProfile() {
		return profile;
	}
	
	//only internal use!
	private final class ResponseRemotable {
		int code;
		boolean cripto;
		Object methodReturn;
		String except;
	}
}