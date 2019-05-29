package net.tarilabs.test20190427midionoff;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Scanner;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

public class App {

    public static void main(String[] args) throws Exception {
        graalvmniworkaround();
        Info[] devs = MidiSystem.getMidiDeviceInfo();
        for (int i = 0; i < devs.length; i++) {
            MidiDevice d = MidiSystem.getMidiDevice(devs[i]);
            printInfo(i, d);
        }
        System.out.print("Index? > ");
        Scanner scanner = new Scanner(System.in);
        int idx = Integer.parseInt(scanner.nextLine());
        MidiDevice device = MidiSystem.getMidiDevice(devs[idx]);
        printInfo(idx, device);
        device.open();
        Transmitter transmitter = device.getTransmitter();
        transmitter.setReceiver(new ConsoleReceiver());
        scanner.nextLine();
        device.close();
    }

    /**
     * https://github.com/oracle/graal/issues/1205
     */
    private static void graalvmniworkaround() throws Exception {
        Field loadedLibs = Class.forName("com.sun.media.sound.Platform").getDeclaredField("loadedLibs");
        loadedLibs.setAccessible(true);
        loadedLibs.setInt(null, 0);
        Method loadLibraries = Class.forName("com.sun.media.sound.Platform").getDeclaredMethod("loadLibraries");
        loadLibraries.setAccessible(true);
        loadLibraries.invoke(null);
        Method readProperties = Class.forName("com.sun.media.sound.Platform").getDeclaredMethod("readProperties");
        readProperties.setAccessible(true);
        readProperties.invoke(null);
    }

    private static void printInfo(int idx, MidiDevice d) {
        Info i = d.getDeviceInfo();
        System.out.println(String.format("%2s %2s %3s %s %s %s %s", idx, d.getMaxTransmitters() != 0 ? "IN" : "", d.getMaxReceivers() != 0 ? "OUT" : "", i.getName(), i.getVendor(), i.getVersion(), i.getDescription()));
    }

    private static final class ConsoleReceiver implements Receiver {

        private static final String[] KEYS = {"Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si"};

        @Override
        public void send(MidiMessage message, long timeStamp) {
            if (message instanceof ShortMessage) {
                ShortMessage s = (ShortMessage) message;
                switch (s.getCommand()) {
                    case 0x80:
                        System.out.print("Off ");
                        break;
                    case 0x90:
                        System.out.print(" On ");
                        break;
                }
                int d1 = s.getData1();
                System.out.print(d1 > 127 ? "?" : KEYS[d1 % 12] + (d1 / 12 - 1));
                System.out.println(" vel: " + s.getData2());
            } else {
                System.out.println(message);
            }
        }

        @Override
        public void close() {
            // nothing (yet).
        }
    }
}
