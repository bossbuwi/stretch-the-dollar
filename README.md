# Getting Started

I am doing development on this app on my SteamDeck. Yes, I have a PC, 
but I also want to try to do software development work on Linux, so SteamDeck it is.
Besides, doing something on SteamDeck other than playing games is cool.

## Pre-requisites
### Default on Steam Deck 
Note: These may or may not be pre-installed on other OS or machines. If they can't be found, then go buy a SteamDeck. Or just search for howtos on the Internet.

- SteamDeck, of course, duh. Suggested SteamOS version is 3.7.x or higher. This is to ensure that distrobox and podman are already pre-installed.
- distrobox 1.8.0 or higher. This is pre-installed on SteamOS 3.7.x. Thanks Gaben.
- podman 5.3.2 or higher. This is pre-installed on SteamOS 3.7.x. Thanks Gaben. 
- Git 2.54.x or higher. This is pre-installed on SteamDeck. Thanks Gaben.
- Console app or something that can execute commands. On SteamDeck, the default is Konsole.

### Needs to be installed
- Java 21 or higher. Try 21 first. I am not really sure that nothing will break on other versions lol.
- IDE, preferably IntelliJ IDEA 2025.x. Older versions do not support nested JUnit tests afaik. Or is it 2023.x? Not sure.
- Maven 3.9.10 or higher.
- Bruno 3.4.2 or higher. Yeah, I am tired with Postman because I just want to test APIs locally, but it is nagging me to log in and use the cloud.
- Supabase CLI 2.104.x or higher.

### Notes
On other machines that is not the SteamDeck, distrobox and podman are not really needed.
These 2 apps are just needed on SteamDeck to run the Supabase CLI which can't really be directly installed on
the SteamDeck due to its immutable OS. Well technically it can be installed, but it will break when the OS updates.
Even that breaking can be prevented, but it requires too much effort just for Supabase CLI.   
   
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
1. Download [Bruno](https://www.usebruno.com/downloads)
2. Select Linux and the `Linux AppImage x86_64` option.
3. Save the downloaded file somewhere. Again, please remember the directory.
4. Go to the directory and double-click the appimage.
5. That's it!

### Installing Supabase CLI
**Note:**   
This is a bit tedious, but I can't find an easier way to make Supabase CLI work on the SteamDeck.

1. Get a glass of water and some snacks, then take a deep breath.
2. Open the console and double check if distrobox is indeed installed on the SteamDeck by `distrobox version`.    
If it is installed, a version number should be returned. If not, then **UPDATE THE STEAMOS** until the command works.
3. Once distrobox is working, check podman availability by `podman -v`.   
If it is installed, a version number should be returned. If not, then **UPDATE THE STEAMOS** until the command works.
4. Once both distrobox and podman are available, create a new Fedora distrobox with `distrobox create --name fedora-box --image fedora:latest`
5. If prompted to do anything, just enter `Y`. You can't proceed with the creation otherwise.  
This is 100% safe. Will an established name like distrobox do malicious things to your system? Probably not!
6. The creation process is done once it says _Distrobox 'fedora-box' successfully created._
7. To enter the newly created box, use `distrobox enter fedora-box`
8. It will do a quick setup and the process is done once it says _Container Setup Complete!_
9. If you enter `pwd`, you'll notice that the command line is at the Deck's home directory. Take note of that for now. **AND DON'T CLOSE THE CONSOLE JUST YET!**
10. Open your browser and download the latest non-beta release (or even a beta if you like living dangerously) of [Supabase CLI](https://github.com/supabase/cli).  
Pick the file with `linux_amd64.rpm` extension. Note the directory where the file is downloaded.
11. Go back to the console from Step 9. Navigate to the directory where the rpm file has been downloaded.
12. Once on the file's directory, install it with `sudo dnf install filename.rpm`
13. It will update and load repositories then show what is actually being installed.  
```
Package      Arch      Version         Repository             Size
supabase     x86_64    0:2.106.0-1     @commandline      199.2 MiB
```
14. It will then ask if it is okay to download additional packages. Of course, say yes with `Y` Don't just hit enter because `N` is the default response.
15. It will commence the downloads and once it says _Complete!_ , then the setup is done.
16. Enter `supabase -v` to check if the process completed successfully.  
If everything is fine, a version number should be returned. If not, then good luck troubleshooting what went wrong. Or just restart from Step 1.  
Note: At this point, the installer (rpm file) can be deleted.

### How to start the Supabase CLI
1. Enter the box with `distobox enter fedora-box`   
Replace _fedora-box_ with the name used during the box creation. This guide assumes _fedora-box_ is used.  
Note: All commands after this step are executed inside the box. The console is inside the box if there is a box icon at the command line.
2. Navigate to the project directory and start supabase with `supabase start`
3. If an error regarding Unix container or podman shows, enter the command `export DOCKER_HOST="unix:///run/user/1000/podman/podman.sock"`
4. Repeat Step 2.
5. Supabase CLI should now start.