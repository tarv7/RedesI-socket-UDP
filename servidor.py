import socket
import json
import sqlite3

def EhInteiro(x):
	try:
		int(x)
		return True
	except ValueError:
		return False

conn = sqlite3.connect('mensagens.db')

cursor = conn.cursor()

cursor.execute("""
CREATE TABLE IF NOT EXISTS mensagens (
	id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
	ip TEXT NOT NULL,
	mensagem TEXT NOT NULL
);
""")

UDP_IP = "192.241.139.196"
UDP_PORT = 5005

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

sock.bind((UDP_IP, UDP_PORT))

while True:
	data, addr = sock.recvfrom(2024)

	print data

	json_data = json.loads(data)
	print 'Metodo: %s\nMensagem: %s\n' % (json_data['metodo'], json_data['mensagem'])

	if json_data['mensagem'] == "broadcast":
		cursor.execute("""
		SELECT mensagem FROM mensagens;
		""")

		respostaAux = cursor.fetchall()
		if not respostaAux:
			resposta = "Nenhuma mensagem cadastrada"
		else:
			resposta = []
			for item in respostaAux:
				resposta.append(str(item[0]))

		aux = ","
		resposta = aux.join(resposta)

	elif(json_data['metodo'] == "post"):
		cursor.executemany("""
		INSERT INTO mensagens (ip, mensagem)
		VALUES (?,?)
		""", [(addr[0], json_data['mensagem'])])

		cursor.execute("""
		SELECT id FROM mensagens WHERE  mensagem = ?;
		""", [(json_data['mensagem'])])

		resposta = cursor.fetchall()[-1][0]

	elif(json_data['metodo'] == "get"):
		if EhInteiro(json_data['mensagem']) == True:
			cursor.execute("""
			SELECT mensagem FROM mensagens WHERE id = ?;
			""", [(int(json_data['mensagem']))])

			resposta = cursor.fetchone()
			if not resposta:
				resposta = "Mensagem nao encontrada"
			else:
				resposta = resposta[0]
		else:
			cursor.execute("""
			SELECT mensagem FROM mensagens WHERE ip = ?;
			""", [(json_data['mensagem'])])

			respostaAux = cursor.fetchall()
			if not respostaAux:
				resposta = "Mensagem nao cadastrada"
			else:
				resposta = []
				for item in respostaAux:
					resposta.append(str(item[0]))

			aux = ","
			resposta = aux.join(resposta)

	sent = sock.sendto(str(resposta), addr)
	print 'enviando %s bytes para %s' % (sent, addr)

	conn.commit()
conn.close()
