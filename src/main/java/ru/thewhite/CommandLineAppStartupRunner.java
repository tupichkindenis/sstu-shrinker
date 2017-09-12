package ru.thewhite;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import org.springframework.util.CollectionUtils;

/**
 * CommandLineAppStartupRunner
 */
@Component
public class CommandLineAppStartupRunner implements CommandLineRunner {

    private String BASE_FOLDER = "data";
    private String NEW_FOLDER = "new";
    private String TEMP_FOLDER = "toUploadFiles";

    private static final Logger logger = LoggerFactory.getLogger(CommandLineAppStartupRunner.class);
    @Override
    public void run(String...args) throws Exception {
        logger.info("Application started with command-line arguments: {} . \n To kill this application, press Ctrl + C.", Arrays.toString(args));

        // Получаем список мест регистрации выгруженных из СЭД
        List<File> offices = CollectionUtils.arrayToList( (new File(NEW_FOLDER)).listFiles(File::isDirectory));
        offices.forEach( officeFolderNew -> {
            List<File> requests = (List<File>) FileUtils.listFiles(new File(NEW_FOLDER + "//" + officeFolderNew.getName()),new WildcardFileFilter("*.json"), null);
            logger.info("Start processing for Office: " + officeFolderNew.getName() + ", messages: " + requests.size() );
            requests.forEach( requestNew -> {
                try {
                    File masterRequest = new File(BASE_FOLDER + "//" + officeFolderNew.getName()+ "//" + requestNew.getName());
                    File temporRequest = new File(TEMP_FOLDER + "//" + officeFolderNew.getName()+ "//" + requestNew.getName());

                    if (masterRequest.exists() == false ){
                        logger.info("-- add," + requestNew.getName() );
                        FileUtils.copyFile(requestNew, masterRequest);
                        FileUtils.copyFile(requestNew, temporRequest);
                    } else {
                        if(FileUtils.contentEquals(requestNew, masterRequest) == false){
                            logger.info("-- update," + requestNew.getName() );
                            FileUtils.copyFile(requestNew, masterRequest);
                            FileUtils.copyFile(requestNew, temporRequest);
                        }else{
                            logger.info("-- skip " + requestNew.getName() );
                        }
                    }
                }catch (IOException ex){
                    logger.error("IOException. " + requestNew.getName(), ex);
                }catch (Exception ex){
                    logger.error("Exception." + requestNew.getName(), ex);
                }
            });
        });
    }
}


