
import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

class Parallel extends Thread {

    String[] links;
    File[] pcPlaces;

   public Parallel(String[] URLs, File[] pcs){
      links = URLs;
      pcPlaces = pcs;
   }
   
    @Override
    public void run() {

        try{
            for(int i = 0; i < links.length; i++) {
                URL url = new URL(links[i]);
                BufferedInputStream inputStream  = new BufferedInputStream(url.openStream());
                FileOutputStream outputStream = new FileOutputStream(pcPlaces[i]);
                byte[] buffer = new byte[1024];
                int reader = 0;
                while ((reader = inputStream.read(buffer, 0, 1024))!=-1){
                    outputStream.write(buffer, 0, reader);
                }
                  

                System.out.print(pcPlaces[i].getName() + " ->done, ");
                inputStream.close();
                outputStream.close();
            }
        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }
}

class Sequential {

    public void downloadFile(String link, File path){

        try{

            URL url = new URL(link);
            BufferedInputStream inputStream  = new BufferedInputStream(url.openStream());
            FileOutputStream outputStream = new FileOutputStream(path);
            byte[] buffer = new byte[1024];
            int reader = 0;
            while ((reader = inputStream.read(buffer, 0, 1024))!=-1){
                outputStream.write(buffer, 0, reader);
            }
            
            System.out.print(path.getName() + " ->done, ");
            inputStream.close();
            outputStream.close();
        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

}


public class Main{


    static File file1 = new File((Paths.get("downloads/file1.pdf").toAbsolutePath().toString()));
    static File file2 = new File((Paths.get("downloads/file2.pdf").toAbsolutePath().toString()));
    static File file3 = new File((Paths.get("downloads/file3.pdf").toAbsolutePath().toString()));
    static File file4 = new File((Paths.get("downloads/file4.pdf").toAbsolutePath().toString()));

    static File [] files ={file1, file2, file3, file4};
    
    public static void main(String[] args) {
        File dir = new File("downloads");
        if (!dir.exists())
            dir.mkdirs();

        int input = Integer.parseInt(args[0]);

        if (input==0){
            long start =System.currentTimeMillis();
            System.out.println(" Mode: Single threaded ...");
            System.out.print("Files: " );
            singleThreaded();
            long end =System.currentTimeMillis();
            System.out.println("Time "+ (end-start)/1000 + "." +(end-start)%1000 + " seconds" );
        }
        if (input==1){
            System.out.println(" Mode: Multi threaded...");
            System.out.print("Files: " );
            multiThreaded();
        }
    }

    public static void singleThreaded(){
        Sequential sequential = new Sequential();
        try(FileReader fileReader =new FileReader("config.txt");
            BufferedReader bufferedReader =new BufferedReader(fileReader)) {
            String str = " ";
            int i = 0;
            while ((str = bufferedReader.readLine()) != null) {
                sequential.downloadFile(str,files[i]);
                i++;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void multiThreaded(){

            Map<String, File> hashMap = new HashMap<>();
            try(FileReader fileReader =new FileReader("config.txt");
                BufferedReader bufferedReader =new BufferedReader(fileReader)) {
            String str = " ";
            int i = 0;
            while ((str=bufferedReader.readLine())!=null){
                hashMap.put(str, files[i]);
                i++;
            }

        }
        catch (Exception ex){
            System.out.println("Exception occurred while reading from file " + ex.getMessage());
        }

        
        int cores = Runtime.getRuntime().availableProcessors();
        Parallel[] parallels = new Parallel[Math.min(cores, hashMap.size())];

   
        int filesPerThread = Math.max(hashMap.size() / parallels.length, 1); 

        int remainder = hashMap.size() % parallels.length;

        long start = System.currentTimeMillis();
        
        for (int i = 0; i < parallels.length; i++)  
        {
            ArrayList<String> urlsForThread = new ArrayList<>();
            ArrayList<File> localPathsForThread = new ArrayList<>();
            
            for (int j = 0; j < filesPerThread; j++) 
            {
                String url = hashMap.keySet().iterator().next();
                File file = hashMap.get(url);
                
                urlsForThread.add(url);
                localPathsForThread.add(file);
                
                hashMap.remove(url);
            }
            
            if (i == parallels.length - 1 && remainder != 0) {   
                for(String url: hashMap.keySet()) {
                    File file = hashMap.get(url);
    
                    urlsForThread.add(url);
                    localPathsForThread.add(file);

    
                    hashMap.remove(url);
                }
            }

                
            parallels[i] = new Parallel(urlsForThread.toArray(String[]::new), localPathsForThread.toArray(File[]::new));
            parallels[i].start();
        }
        
        try{
            for (Parallel parallel : parallels)
            {
                parallel.join();
            }

            long end =System.currentTimeMillis();
            System.out.println("Time "+ (end-start)/1000 + "." +(end-start)%1000 + " seconds" );
        }

        catch (InterruptedException iex){
            System.out.println(iex.getMessage());
        }
    }
}


