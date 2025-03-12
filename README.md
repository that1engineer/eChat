# eChat
This is a simple chat application I made that allows multiple users to send messages to each other from separate devices sharing a network.
The "e" stands for "Ethan".

## How to use it
1. Download the files and ensure they remain in one folder.
2. Open eChat.java in your favorite code editor, and be sure to open the folder that the files reside in (in VS Code, it's "Ctrl+K Ctrl+O").
3. In a terminal, download Telnet by running **"dism /online /Enable-Feature /FeatureName:TelnetClient"**.
4. Navigate to the correct directory where the java files are located and compile the code by typing **"javac eChat.java"**.
5. Next, start the server by entering **"java eChat *>PORT #<* *>MAX USERS<*"**. I recommend using port *"12345"* since open_clients.bat will automatically open two instances of the project using that port for local testing, but any valid port works.
6. To use with other devices, enter **"telnet *>YOUR IP ADDRESS<* *>PORT #<*"** after starting the server. NOTE: Users must be connected to the same WiFi source.
