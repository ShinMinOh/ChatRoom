package server;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain
{
	static ServerDB DB = new ServerDB();
	public static void main(String[] args) throws Exception
	{
		/*서버가 가동되면, 서버가 가동되었다는 메시지를 서버에 출력 한 뒤
		 * 쓰레드풀을 이용하여 소켓 연결을 대기함
		 * accept 되었을 시, 쓰레드풀을 이용하여 Handler를 실행*/
		System.out.println("The chat server is running...");
		ExecutorService pool = Executors.newFixedThreadPool(500);
		try (ServerSocket listener = new ServerSocket(59001))
		{
			while (true)
			{
				pool.execute(new Handler(listener.accept()));
			}
		}
	}

	private static class Handler implements Runnable
	{
		private Socket socket;
		private Scanner in;
		private PrintWriter out;

		public Handler(Socket socket)
		{
			this.socket = socket;
		}

		public void run()
		{
			try
			{
				in = new Scanner(socket.getInputStream());
				out = new PrintWriter(socket.getOutputStream(), true);
				
				/*클라이언트와 연결되면, 클라이언트로 CONNECTED라는 문자열을 보냄*/
				out.println("CONNECTED");
				
				while (true)
				{
					String input = in.nextLine();
					
					/*클라이언트로부터 CONNECTED라는 문자열을 받으면,
					 * 서버에 CONNECTED라는 문자열과 클라이언트의 아이피주소를 출력*/
					if (input.startsWith("CONNECETED"))
					{
						System.out.println(input);
					}
					
					/*클라이언트로부터 로그인 요청을 받으면,
					 * 받은 문자열을 파싱하여 아이디와 비밀번호를 저장한 뒤
					 * DB로 아이디와 패스워드를 보냄
					 * 로그인 성공시 LOGINOK를, 실패시 LOGINFAIL이라는 문자열을
					 * 클라이언트로 전송*/
					if (input.startsWith("LOGIN"))
					{
						System.out.println(input);
						String[] loginArray = input.split(" ");
						String loginId = loginArray[1];
						String loginPassword = loginArray[2];
						boolean check = DB.login(DB, loginId, loginPassword);
						
						/*로그인이 확인 되면 로그인 날짜를 저장
						 * 친구 리스트를 DB로 부터 불러온 뒤, LOGINOK라는 문자열과 함께
						 * 친구 리스트를 보내줌*/
						if (check)
						{
							SimpleDateFormat format1 = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
							Date time = new Date();
							String loginTime = format1.format(time);
							User.add_participants(loginId, out);
							User.add_socket(loginId, socket);
							User.add_loginTime(loginId, loginTime);
							String friendList = DB.findFriend(DB, loginId);
							String[] friend = friendList.split(" ");
							String friendSend = "";
							for (int i = 0; i < friend.length; i++)
							{
								String findFriend = friend[i];
								PrintWriter isOnline = null;
								friendSend += friend[i] + " ";
							}
								out.println("LOGINOK " + friendSend);
							
							
						}
						else
						{
							out.println("LOGINFAIL");
						}
					}
					
					/*클라이언트로부터 비밀번호 찾기 요청이 들어왔을 시
					 * 받은 문자열을 파싱하여 아이디와 이메일을 저장한 뒤
					 * DB로 아이디와 이메일을 보냄
					 * 실패시 FINDPWFAIL을, 성공시 FINDPWOK와 비밀번호를
					 * 클라이언트로 보냄*/
					if (input.startsWith("FINDPW"))
					{
						System.out.println(input);
						String[] loginArray = input.split(" ");
						String findPwId = loginArray[1];
						String findPwEmail = loginArray[2];
						String check = DB.findPw(DB, findPwId, findPwEmail);
						
						if (check.isEmpty())
						{
							out.println("FINDPWFAIL");
						}
						else
						{
							out.println("FINDPWOK " + check);
						}
					}
					
					/*클라이언트로부터 회원가입 요청이 들어왔을 시
					 * 받은 문자열을 DB로 보냄*/
					if (input.startsWith("REGISTER"))
					{
						DB.register(DB, input);
						System.out.println(input);
					}
					
					/*클라이언트로부터 친구 검색 요청이 왔을 시
					 * DB에서 검색 한 뒤, 결과를 보내줌*/
					if (input.startsWith("SEARCH"))
					{
						String[] inp = input.split(" ");
						String inpName = inp[1];
						String result = DB.Search(DB, inpName);
						out.println("SEARCHRESULT " + result);
					}
					
					/*클라이언트로부터 친구 추가 요청이 왔을 시
					 * DB를 이용하여 친구를 추가함*/
					if (input.startsWith("ADDFRIEND"))
					{
						String[] inp = input.split(" ");
						String inpName = inp[1];
						String addName = inp[2];
						DB.AddFriend(DB, inpName, addName);
					}
					
					/*클라이언트로부터 상태 메시지 또는 닉네임 변경 요청이 왔을 시
					 * DB를 이용하여 닉네임을 바꿔주고, 상태 메시지 변경을 서버에 반영함*/
					if (input.startsWith("CHANGEINFO"))
					{
						String[] inp = input.split(" ");
						String id = inp[1];
						String status = inp[2];
						String nickName = inp[3];
						
						User.add_statusMessage(id, status);
						DB.changeNick(DB, id, nickName);
					}
					
					/*클라이언트로부터 유저 정보를 보여달라는 요청이 왔을 시,
					 * 서버에서 상태 메시지와 로그인 시간을 불러옴*/
					if (input.startsWith("SHOWINFO"))
					{
						String[] inp = input.split(" ");
						String id = inp[1];
						
						String status = User.status_message.get(id);
						String loginTime = User.login_time.get(id);
						boolean isOnline = User.participants.containsKey(id);
						if (isOnline)
						{
							out.println("INFOS " + status + " " + loginTime + " " + "online");
						}
						else
						{
							out.println("INFOS " + status + " " + loginTime + " " + " " + "offline");
						}
					}
					
					/*클라이언트로부터 전체 채팅방 참여 요청이 왔을 시,
					 * ENTERALL이라는 문자열을 클라이언트로 보냄*/
					if (input.startsWith("ENTERALL"))
					{
						out.println("ENTERALL");
					}
					
					/*클라이언트가 전체 채팅방에 메시지를 보냈을 때
					 * MESSAGEFROM 문자열과 함께 보낸 사람과 메시지를 각 클라이언트로 전송*/
					if (input.startsWith("MESSAGE"))
					{
						String[] inp = input.split(" ");
						String id = inp[1];
						String message = inp[2];
						for (PrintWriter p : User.participants.values())
						{
							p.println("MESSAGEFROM " + id + " " + message);
						}
					}
					
					/*클라이언트가 1대1 채팅방 개설 요청을 하였을 시
					 * 신청자에게는 FOPENF라는 문자열과 아이디를,
					 * 피신청자에게는 OPEN이라는 문자열과 아이디를 보내줌*/
					if (input.startsWith("CHATWITH"))
					{
						String[] inp = input.split(" ");
						String id1 = inp[1];
						String id2 = inp[2];
						PrintWriter id1p = User.participants.get(id1);
						PrintWriter id2p = User.participants.get(id2);
						id1p.println("FOPENF " + id2 + " " + id1);
						id2p.println("OPEN " + id1 + " " + id2);
					}
					
					/*1대1 채팅방에 대해 피신청자의 응답이 왔을 시
					 * 개설을 거부하면 CHAT2NO와 rejected라는 문자열을,
					 * 개설을 허용하면 CHAT2OK와 entered라는 문자열을 보내줌*/
					if (input.startsWith("CHATNO") || input.startsWith("CHATOK"))
					{
						String[] ids = input.split(" ");
						String id1 = ids[1];
						String id2 = ids[2];
						PrintWriter id1p = User.participants.get(id1);
						PrintWriter id2p = User.participants.get(id2);
						
						if (input.startsWith("CHATOK"))
						{
							id1p.println("CHAT2OK " + id1 + " " + id2 + " " + "entered");
						}
						if (input.startsWith("CHATNO"))
						{
							id1p.println("CHAT2NO " + id1 + " " + id2 + " " + "rejected");
						}
					}
					
					/*1대1 채팅방에서 메시지 전송 요청이 왔을 시
					 * 해당하는 사용자에게 CHAT2WITH이라는 문자열과 함께
					 * 메시지를 보내줌*/
					if (input.startsWith("CHAT2"))
					{
						String[] inp = input.split(" ");
						String from = inp[1];
						String to = inp[2];
						String message = inp[3];
						PrintWriter fromp = User.participants.get(inp[1]);
						PrintWriter top = User.participants.get(inp[2]);
						top.println("CHAT2WITH " + from + " " + message);
					}
				}
			}
			catch (Exception e)
			{
				System.out.println(e);
			}
		}
	}
}