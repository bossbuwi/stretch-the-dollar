---
layout: default
title: Home
---

### Below instructions are no  longer updated. New instructions are still being made. Please be patient.

# Getting Started

I am doing development on this app on my SteamDeck. Yes, I have a PC,
but I also want to try to do software development work on Linux, so SteamDeck it is.
Besides, doing something on SteamDeck other than playing games is cool.

## Pre-requisites
### Default on Steam Deck
Note: These may or may not be pre-installed on other OS or machines. If they can't be found, then go buy a SteamDeck. Or just search for how tos on the Internet.

- SteamDeck, of course, duh. Suggested SteamOS version is 3.7.x or higher. This is to ensure that distrobox and podman are already pre-installed.
- ~~distrobox 1.8.0 or higher. This is pre-installed on SteamOS 3.7.x. Thanks Gaben.~~
- ~~podman 5.3.2 or higher. This is pre-installed on SteamOS 3.7.x. Thanks Gaben.~~
- Git 2.54.x or higher. This is pre-installed on SteamDeck. Thanks Gaben.
- Console app or something that can execute commands. On SteamDeck, the default is Konsole.

### Needs to be installed
- Java 21 or higher. Try 21 first. I am not really sure that nothing will break on other versions lol.
- IDE, preferably IntelliJ IDEA 2025.x. Older versions do not support nested JUnit tests afaik. Or is it 2023.x? Not sure.
- Maven 3.9.10 or higher.
- Bruno 3.4.2 or higher. Yeah, I am tired with Postman because I just want to test APIs locally, but it is nagging me to log in and use the cloud.
- ~~Supabase CLI 2.104.x or higher.~~

### Notes
~~On other machines that is not the SteamDeck, distrobox and podman are not really needed.
These 2 apps are just needed on SteamDeck to run the Supabase CLI which can't really be directly installed on
the SteamDeck due to its immutable OS. Well technically it can be installed, but it will break when the OS updates.
Even that breaking can be prevented, but it requires too much effort just for Supabase CLI.~~

Also note that everything on this guide needs to be done while on SteamDeck's desktop mode. If for some reason you managed to do everything on this guide while on gaming mode, then go apply at Valve. Gaben needs you.

### Installing Java
1. Download the appropriate package from here [Azul JDK](https://www.azul.com/downloads/?package=jdk#zulu).For the SteamDeck, it should be Linux x86 64-bit. Choose the ZIP file option.
2. Extract the contents somewhere and note the directory.
3. Open file `.bashrc` on `~` (this is the Deck's home directory)
4. Add `export JAVA_HOME=/directory/from/step2`
5. Add `export PATH=$PATH:$JAVA_HOME/bin` below it.
6. Save the file and restart the console.
7. Run `java -version`. If the setup is correct, the Java version should be returned.

### Installing IntelliJ IDEA
1. Download from [IntelliJ Downloads](https://www.jetbrains.com/idea/download/?section=linux)
2. Select the `Linux tar.gz` file option.
3. Extract the file contents somewhere and note the directory. You can't work if you forget where it is.
4. To run the IDE, go to the directory from Step 3 and run the `idea` file.
5. Step 4 is definitely a pain to do everytime so, go to the Desktop folder and create a new file named `IntelliJ.desktop`
6. Open the file on a text editor (Kate is the default on SteamDeck).
7. Paste this sketchy manifest (not a virus):
```[Desktop Entry]
Version=1.0
Type=Application
Name=IntelliJ
Exec=/directory-from-step-3/bin/idea
Icon=/directory-from-step-3/bin/idea.png
Terminal=false
```
8. Right-click on the `Intellij.desktop` file and select Properties.
9. Go to the Permissions tab and tick the `Allow executing file as program` and click OK.

or if you are feeling techie, (considering you are using the SteamDeck for this, you are):

8. Open the console and go to the Desktop directory by `cd ~/Desktop`
9. Add execute permission to the file with `chmod +x IntelliJ.desktop`

whatever you choose, the next step is:

10. Double-click the newly created shortcut and see if it indeed launches IntelliJ.

### Installing Maven
1. Download [Maven](https://maven.apache.org/download.cgi)
2. Select the `Binary tar.gz` option. Or even the `Source tar.gz` if you are feeling adventurous.
3. Extract the contents somewhere and note the directory. This is the third time, I really hope you are not losing those directories.
4. Open the `.bashrc` file (See [Installing Java](#installing-java)).
5. Add `export M2_HOME=/directory-from-step-3/apache-maven-3.9.10/`
6. Add `export PATH=$PATH:$M2_HOME/bin` below it.
7. Restart the console and enter `mvn -v`
8. If Maven is successfully installed, then the version number should be returned.

### Installing Bruno
1. Download Bruno from the Steam Deck's Software Center.
2. ~~Select Linux and the `Linux AppImage x86_64` option.~~
3. ~~Save the downloaded file somewhere. Again, please remember the directory.~~
4. ~~Go to the directory and double-click the appimage.~~
5. That's it!

### ~~Installing Supabase CLI~~
~~**Note:**~~   
~~This is a bit tedious, but I can't find an easier way to make Supabase CLI work on the SteamDeck.~~

1. ~~Get a glass of water and some snacks, then take a deep breath.~~
2. ~~Open the console and double check if distrobox is indeed installed on the SteamDeck by `distrobox version`.~~    
   ~~If it is installed, a version number should be returned. If not, then **UPDATE THE STEAMOS** until the command works.~~
3. ~~Once distrobox is working, check podman availability by `podman -v`.~~   
   ~~If it is installed, a version number should be returned. If not, then **UPDATE THE STEAMOS** until the command works.~~
4. ~~Once both distrobox and podman are available, create a new Fedora distrobox with `distrobox create --name fedora-box --image fedora:latest`~~
5. ~~If prompted to do anything, just enter `Y`. You can't proceed with the creation otherwise.~~  
   ~~This is 100% safe. Will an established name like distrobox do malicious things to your system? Probably not!~~
6. ~~The creation process is done once it says _Distrobox 'fedora-box' successfully created._~~
7. ~~To enter the newly created box, use `distrobox enter fedora-box`~~
8. ~~It will do a quick setup and the process is done once it says _Container Setup Complete!_~~
9. ~~If you enter `pwd`, you'll notice that the command line is at the Deck's home directory. Take note of that for now. **AND DON'T CLOSE THE CONSOLE JUST YET!**~~
10. ~~Open your browser and download the latest non-beta release (or even a beta if you like living dangerously) of [Supabase CLI](https://github.com/supabase/cli).~~  
    ~~Pick the file with `linux_amd64.rpm` extension. Note the directory where the file is downloaded.~~
11. ~~Go back to the console from Step 9. Navigate to the directory where the rpm file has been downloaded.~~
12. ~~Once on the file's directory, install it with `sudo dnf install filename.rpm`~~
13. ~~It will update and load repositories then show what is actually being installed.~~
```
Package      Arch      Version         Repository             Size
supabase     x86_64    0:2.106.0-1     @commandline      199.2 MiB
```
14. ~~It will then ask if it is okay to download additional packages. Of course, say yes with `Y` Don't just hit enter because `N` is the default response.~~
15. ~~It will commence the downloads and once it says _Complete!_ , then the setup is done.~~
16. ~~Enter `supabase -v` to check if the process completed successfully.~~  
    ~~If everything is fine, a version number should be returned. If not, then good luck troubleshooting what went wrong. Or just restart from Step 1.~~  
    ~~Note: At this point, the installer (rpm file) can be deleted.~~

### ~~How to start the Supabase CLI~~
1. ~~Enter the box with `distrobox enter fedora-box`~~   
   ~~Replace _fedora-box_ with the name used during the box creation. This guide assumes _fedora-box_ is used.~~  
   ~~Note: All commands after this step are executed inside the box. The console is inside the box if there is a box icon at the command line.~~
2. ~~Navigate to the project directory and start supabase with `supabase start`~~
3. ~~If an error regarding Unix container or podman shows, enter the command `export DOCKER_HOST="unix:///run/user/1000/podman/podman.sock"`~~
4. ~~Repeat Step 2.~~
5. ~~Supabase CLI should now start.~~

### Creating a Google Cloud VM
1. Open Google Cloud and search for Compute Engine.
2. Look for the Virtual Machines group on the left navigation bar and click VM instances.
3. Click the Create Instance button to start the VM creation. A new process will open.
4. On the Machine configuration section, do the following:
```
Name: database-test
Region: us-central1(Iowa)
Zone: Any
* Tick the General Purpose tab.
* Select E2.
* On Machine Type, select Preset.
* On the drop down, select Shared Core > e2-micro
Provisioning Model: Standard
```
5. On the OS and storage section, do the following:
```
* Click the Change button to open the Boot disk choices.
* Under Public images 
    > Operating System: Ubuntu
    > Version: Select the latest LTS for x86/64
    > Boot disk type: Standard persistent disk
    > Size: 10GB minimum, 30GB maximum
* Click Select button at the bottom of the screen to apply the choices.
```
6. On the Data protection section, do the following:
```
* Backups: No backup
```
7. On the Networking section, do the following:
```
* Under Firewall, tick Allow HTTP traffic.
* Under Firewall, tick Allow HTTPS traffic.
```
8. On the price estimate at the right, the price should say $6.11 per month or about $0.01 per hour.
9. Click Create button to finalize and create the VM.

### Preparing the new VM
1. To prevent an irritating bug with the new sudo-rs (Rust) implementation on Ubuntu, enter the command below before doing any commands that require __sudo__.
```
sudo update-alternatives --set sudo /usr/bin/sudo.ws
```
This only needs to be entered once per session.
2. Minimal Linux images usually strips `nano utility` to "minimize" the image. Run `sudo apt install nano -y` to install. This is needed to open and edit config files.

### Installing Postgres on the VM
1. Go to Google Cloud > Compute Engine > Virtual machines > VM instances.
2. On the VM list, find the VM created on the previous section 
and click the SSH button on the right hand side under the Connect column.
3. Run command sudo apt update to update the system's package list.
4. Run sudo apt install PostgreSQL postgresql-contrib -y to install the Postgres database server
together with some useful extensions.
5. After the installation is finished, check if the Postgres service is okay, run either  
```
sudo systemctl status postgresql

or

pg_lsclusters
```
You should see `active (exited)` if you run the first one or the actual `postgres cluster` if you run the second one.

### Securing the Postgres Database
1. The __postgres__ user is the default superuser. By default, there is no password. A __strong__ password needs to be set.
To start, `run sudo -i -u postgres` to switch to the Postgres user.
2. Once the command line shows _postgres@vm-name_, run `psql`. The terminal should now show the __postgres=#__ prompt.
3. Set a new, hopefully strong password using:
```
ALTER USER postgres WITH PASSWORD 'HopefullyStrongPassword';
```
__Note:__  
Please take this seriously. Replace the __HopefullyStrongPassword__ with a legit and strong password.
I once became a victim of a database ransomware attack because I set my password to _password_.
Good thing that that database is just a test database for some side studies. __Take internet security seriously.__
There are lots of malicious entities out there.
4. The prompt should quickly show _ALTER ROLE_. Upon regaining the ability to type, enter `\q` to exit the Postgres prompt.

### Configure Postgres to listen for remote connections
By default, PostgreSQL only listens for connections from localhost.
It needs to listen on all available network interfaces. These series of commands need to be run outside the Postgres prompt.
These instructions are executed at the VM's console, unless specified otherwise.
If the VM's terminal still shows __postgres=#__, enter `\q` to exit.
1. The Postgres versions needs to be known first. Enter `pg_lsclusters` and note the number on the directory under the `Data directory` column.
2. Run `sudo nano /etc/postgresql/<number>/main/postgresql.conf`
3. Use Ctrl + F and find `listen_addresses`
4. Remove the comment # and change from `'localhost'` to `'*'`.
5. Use Ctrl + O to write out the changes. Press enter if asked.
6. Use Ctrl + X to exit.
7. Configure `pg_hba.conf` for secure authentication. Open the file with `sudo nano /etc/postgresql/<number>/main/pg_hba.conf`
8. Scroll to the bottom of the file and add the following:
```
# Allow connections from your local machine via the SSH tunnel, using a strong password
host    all             all             127.0.0.1/32            scram-sha-256
```
9. Use Ctrl + O to write out the changes. Press enter if asked.
10. Use Ctrl + X to exit.
11. Restart Postgres to apply the changes. Run `sudo systemctl restart postgresql`
12. If prompted, run `sudo systemctl daemon-reload` to refresh the system cache.
13. Restart Postgres again if the system cache was refreshed. Run `sudo systemctl restart postgresql`

### Creating a new Postgres user
These instructions are to be executed at the VM's console, unless specified otherwise.
1. Run `sudo -u postgres psql` to enter the Postgres shell. The prompt should change to `postgres=#`.
2. Run the command to create a user. Remember to create a strong password.
```
CREATE USER admin WITH PASSWORD 'StrongAdminPassword123!';
```
3. Give the new user superuser privileges (yep, not the best practice but this is the  easiest for a learning project like this.)
```
ALTER USER admin WITH SUPERUSER;
```
4. Enter `\q` to exit the Postgres shell.

### Creating the test database
These instructions are to be executed at the VM's console, unless specified otherwise.
1. Run `sudo -u postgres psql` to enter the Postgres shell. The prompt should change to `postgres=#`.
2. Run command to create a table.
```
CREATE DATABASE test;
```
3. Make admin the owner.
```
ALTER DATABASE test OWNER TO admin;
```


### Setting up an SSH tunnel (Steam Deck)
1. Open a terminal (usually Konsole) and enter `ssh-keygen -t rsa -f ~/.ssh/gcp_key -C paradoxhorizondevs`.
This will generate an SSH key on the Steam Deck on the specific directory (_~/.ssh/_). 
2. Print the public key using `cat ~/.ssh/gcp_key.pub`. Copy the entire output that starts from ssh-rsa and ends with the username.
3. Go to Google Cloud Console. Open Compute Engine > Settings > Metadata. Select the SSH tab and paste the string from step 2 on the SSH-key textbox.
4. Save.
5. Go to the VM list on Compute Engine > Virtual machines > VM instances. Note the VM's external IP address.
6. Open an SSH connection window to the VM by clicking the SSH button. Note the username on the console.
7. Connect to the VM from the Steam Deck using `ssh -i ~/.ssh/gcp_key -L 5432:localhost:5432 username@EXTERNAL_IP`
8. Keep the terminal running on the Steam Deck. This will serve as the tunnel that the Spring Boot app on IntelliJ will use.

### Setting up environment variables on IntelliJ
1. Search and install the `EnvFile` plugin from the IntelliJ marketplace.
2. Duplicate the .env.template on the repository root. Rename the new file as `.env`. Remember to not include the .env file in the commits.
3. Edit the .env file and put the following:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/test
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=password_from_admin_user_creation
```
4. Go to Run > Run.. > Edit Configurations.
5. Select the application configuration being used for development.
6. Tick the `Enable Envfile`.
7. Then on the list below, click the `+` icon, select .env file and add the .evn file from step 2. 
If the file cannot be seen, just click the __eye__ icon near the top of the dialog box.
8. Save the configuration.

### Setting up a custom port
Note:  
By default, Spring Boot attempts to start the app on port 8080. But, the SteamDeck uses port 8080 while the Steam app is running.  
This prevents the app from starting due to the port conflict.  
The Steam app can be stopped, but it isn't really a **Steam**Deck without **Steam** right?

1. Duplicate the `application.properties` file inside `src/main/resources`.
2. Rename the new file `application-local.properties`.
3. Modify the `server.port` on the local properties file to an unused port, e.g. 8085.
4. Open `Run | Edit Configurations..`.
5. Select the Spring Boot app config.
6. Click the `Modify options` and tick the `Program arguments` item. A new text box should appear.
7. On the new text box, add `--spring.profiles.active=local`.
8. Click `Apply` then `OK`.
9. Start the app and check on the logs if the port used is indeed the one set on the local properties file.