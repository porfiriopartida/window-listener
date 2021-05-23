package com.porfiriopartida.screen.application;

import com.porfiriopartida.exception.ConfigurationValidationException;
import com.porfiriopartida.obsdeck.IScreenApplication;
import com.porfiriopartida.obsdeck.transition.TransitionFacade;
import com.porfiriopartida.screen.config.ScreenDetectionCommand;
import com.porfiriopartida.screen.jna.WindowListener;
import com.porfiriopartida.screen.utils.CSVUtil;

import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public abstract class ScreenApplication implements IScreenApplication, Observer {
    private String configFile = null;
    private TransitionFacade transitionFacade;

    @Override
    public String getConfigFile() {
        return configFile;
    }

    @Override
    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public void start() throws IOException, ConfigurationValidationException {
        WindowListener windowListener = new WindowListener();
        windowListener.addObserver(this);

        transitionFacade = new TransitionFacade();

        List<ScreenDetectionCommand> screenDetectionCommands = CSVUtil.fromCsv(configFile);
        transitionFacade.addTransitionCommands(screenDetectionCommands);

        new Thread(windowListener).start();
    }
    public String getCommand(String windowName){
        return transitionFacade.getSceneNameByKey(windowName);
    }

    /**
     * Prints the command specified in your CSV configuration file.
     *
     * @param args Expects --file /absolute/path/configuration.csv
     */
    public static void main(String[] args) throws IOException, ConfigurationValidationException {
        String externalConfigFile = null;
        for (int i = 0; i < args.length; i++) {
            if("--file".equals(args[i])){
                externalConfigFile = args[i + 1];
            }
        }
        if(externalConfigFile == null){
            externalConfigFile = ScreenApplication.class.getClassLoader().getResource( "configuration.csv" ).getPath();
        }

        String unknownCommand = "What are you doing?";
        ScreenApplication soutApplication = new ScreenApplication() {
            @Override
            public void update(Observable o, Object arg) {
                String newWindow = arg.toString();
                String command = this.getCommand(newWindow);
                if(command == null){
                    command = unknownCommand;
                }

                System.out.println(command);
            }
        };

        soutApplication.setConfigFile(externalConfigFile);
        soutApplication.start();
    }
}
