package impl.client;

import impl.Configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import rso.at.EntryNotFound;
import rso.at.FileEntry;
import rso.at.FileType;
import rso.at.HostNotPermitted;
import rso.at.InvalidOperation;

/**
 * 
 * @author Daniel Pogrebniak
 * 
 */
public class Client {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Error: No arguments!");
            System.out.println(help());
            System.exit(10);
        }
        Client client = new Client();
        FileSystem fs = null;
        try {
            Configuration.load();
            fs = new FileSystemImpl();
        } catch (NumberFormatException e1) {
            System.err.println(e1.getMessage());
            System.exit(11);
        } catch (FileNotFoundException e1) {
            System.err.println("Error: Could not find configuration file!");
            System.exit(12);
        } catch (IOException e1) {
            System.err.println("Error: Could not read configuration file!");
            System.exit(13);
        } catch (InvalidOperation e) {
            System.err.println("Error(" + e.code + ") " + e.message);
            System.exit(14);
        }
        try {
            fs.connect();
            int returnCode = client.selectAction(args, fs);
            if (returnCode != 0) {
                System.exit(returnCode);
            }
        } catch (TTransportException e) {
            System.out
                    .println("Error: Could not establish connection to any server!");
            System.exit(14);
        } finally {
            fs.disconnect();
        }
        System.exit(0);
    }

    private int selectAction(String[] args, FileSystem fs) {
        String action = null;
        if (args[0].startsWith("-"))
            action = args[0].substring(1);
        else
            action = args[0];
        try {
            switch (action) {
            case "ls":
                if (args.length == 1) {
                    showListOfEntries(fs.lookup("/"));
                } else if (args.length == 2) {
                    showListOfEntries(fs.lookup(args[1]));
                } else {
                    throw new InvalidOperation(16,
                            "Wrong number of arguments in \"ls\" call");
                }
                System.out.println("Done.");
                break;
            case "mkdir":
                if (args.length == 2) {
                    fs.makeDirectory(args[1]);
                } else if (args.length == 3) {
                    fs.makeDirectory(fs.getFileEntry(args[1]), args[2]);
                } else {
                    throw new InvalidOperation(16,
                            "Wrong number of arguments in \"mkdir\" call");
                }
                System.out.println("Done.");
                break;
            case "mkfile":
                if (args.length == 2) {
                    fs.makeFile(args[1]);
                } else if (args.length == 4) {
                    Integer size = new Integer(args[3]);
                    fs.makeFile(fs.getFileEntry(args[1]), args[2], size);
                } else {
                    throw new InvalidOperation(16,
                            "Wrong number of arguments in \"mkfile\" call");
                }
                System.out.println("Done.");
                break;
            case "rm":
                if (args.length == 2) {
                    fs.removeEntry(args[1]);
                } else {
                    throw new InvalidOperation(16,
                            "Wrong number of arguments in \"rm\" call");
                }
                System.out.println("Done.");
                break;
            case "mv":
                if (args.length == 3) {
                    fs.moveEntry(args[1], args[2]);
                } else {
                    throw new InvalidOperation(16,
                            "Wrong number of arguments in \"mv\" call");
                }
                System.out.println("Done.");
                break;
            case "readPart":
                if (args.length == 4) {
                    long offset = new Long(args[2]);
                    long num = new Long(args[3]);
                    byte[] bytes = fs.readFromFile(args[1], offset, num);
                    System.out.println(new String(bytes, "UTF-8"));
                } else {
                    throw new InvalidOperation(16,
                            "Wrong number of arguments in \"readPart\" call");
                }
                break;
            case "readAll":
                if (args.length == 3) {
                    readFile(fs, args[1], args[2]);
                } else {
                    throw new InvalidOperation(16,
                            "Wrong number of arguments in \"readAll\" call");
                }
                System.out.println("Done.");
                break;
            case "writePart":
                if (args.length == 4) {
                    long offset2 = new Long(args[2]);
                    fs.writeToFile(args[1], offset2, args[3].getBytes());
                } else {
                    throw new InvalidOperation(16,
                            "Wrong number of arguments in \"writePart\" call");
                }
                System.out.println("Done.");
                break;
            case "writeAll":
                if (args.length == 3) {
                    try {
                        fs.getFileEntry(args[1]);
                    } catch (EntryNotFound e) {
                        fs.makeFile(args[1]);
                    }
                    writeFile(fs, args[1], args[2]);
                } else {
                    throw new InvalidOperation(16,
                            "Wrong number of arguments in \"writeAll\" call");
                }
                System.out.println("Done.");
                break;
            case "help":
                System.out.println(help());
                break;
            default:
                throw new InvalidOperation(16, "Wrong arguments");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return 14;
        } catch (EntryNotFound e) {
            System.out.println(e.getMessage());
            return 15;
        } catch (InvalidOperation e) {
            System.out.println(e.getMessage());
            return 16;
        } catch (HostNotPermitted e) {
            System.out.println("Server nr " + e.currentServerID
                    + " is not permitted to handle action.");
            return 17;
        } catch (TException e) {
            System.out.println(e.getMessage());
            return 18;
        }
        return 0;
    }

    private void showListOfEntries(List<FileEntry> entries) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if (entries.isEmpty()) {
            System.out.println("Chosen folder is empty");
        }
        for (FileEntry entry : entries) {
            Date modificationTime = new Date(
                    (long) entry.getModificationTime() * 1000);
            sb.append(format.format(modificationTime));
            if (entry.getType().equals(FileType.DIRECTORY))
                sb.append(" DIR  ");
            else
                sb.append(" FILE ");
            sb.append(entry.getSize());
            sb.append("\t");
            sb.append(entry.getName());
            sb.append("\n");
        }
        System.out.print(sb.toString());
    }

    private void readFile(FileSystem fs, String from, String to)
            throws IOException, EntryNotFound, InvalidOperation, TException {
        FileEntry entry = fs.getFileEntry(from);
        long size = entry.getSize();
        byte[] bytes = fs.readFromFile(entry, 0, size);
        File file = new File(to);
        if (!file.exists())
            file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bytes);
        fos.close();
    }

    private void writeFile(FileSystem fs, String to, String from)
            throws IOException, EntryNotFound, InvalidOperation, TException {
        File file = new File(from);
        if (!file.exists() || !file.isFile()) {
            throw new IOException(
                    "Chosen file has not filetype or doesnt exist");
        }

        FileEntry entry = fs.allocateFile(to, file.length());
        byte[] bytes = Files.readAllBytes(file.toPath());
        fs.writeToFile(entry, 0, bytes);
    }

    private static String help() {
        return "Available commands:\n"
                + "- ls <directory> ......................List directory's entries\n"
                + "- mkdir <directory>....................Create new directory\n"
                + "- mkfile <path>........................Create new empty file\n"
                + "- rm <path>............................Remove a directory or a file\n"
                + "- mv <currentPath> <newPath>...........Move a directory or a path\n"
                + "- readPart <path> <offset> <size>......Read file fragment\n"
                + "- readAll <remotePath> <localPath>.....Download file from a server\n"
                + "- writePart <path> <offset> <data>.....Modify file on a server\n"
                + "- writeAll <remotePath> <localPath>....Save local file on a server\n"
                + "- help.................................Help";
    }
}
