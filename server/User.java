package server;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

/*현재 접속중인 유저의 정보를 담기위한 클래스*/
public class User
{
	 public static HashMap<String, PrintWriter> participants = new HashMap<>();
	 public static HashMap<String, Socket> participants_socket = new HashMap<>(); 
	 public static HashMap<String, String> login_time = new HashMap<>(); 
	 public static HashMap<String, String> logout_time = new HashMap<>();
	 public static HashMap<String, String> status_message = new HashMap<>(); 
	 
	 //key를 아이디로 하는 해쉬맵에 각각 상태를 넣기 위한 메소드
	 //스트림, 소켓, 로그인 시간, 상태 메시지를 보관함
	 public static void add_participants(String id, PrintWriter out)
	 {
		 participants.put(id, out);
	 }
	 
	 public static void add_socket(String id, Socket socket)
	 {
		 participants_socket.put(id, socket);
	 }
	 
	 public static void add_loginTime(String id, String loginTime)
	 {
		 login_time.put(id, loginTime);
	 }
	 
	 public static void add_logoutTime(String id, String logoutTime)
	 {
		 logout_time.put(id, logoutTime);
	 }
	 
	 public static void add_statusMessage(String id, String statusMessage)
	 {
		 status_message.put(id, statusMessage);
	 }
	 
		//해쉬맵에서 value로 key를 찾기 위한 메소드
		public static Object getKey(HashMap<String, PrintWriter>in, Object value)
		{
			for (Object o : in.keySet())
			{
				if (in.get(o).equals(value))
				{
					return o;
				}
			}
			return null;
		}
	
		//유저가 온라인인지 찾기 위한 메소드
	public static boolean isOnline(HashMap<String, PrintWriter>map, String id)
	{
		if (map.containsKey(id))
		{
			return true;
		}
		return false;
	}
		
}
