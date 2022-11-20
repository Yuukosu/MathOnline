package net.yuukosu;

import lombok.Getter;

import java.io.InputStream;
import java.util.InputMismatchException;
import java.util.Scanner;

public class OptionSelector {

    @Getter
    private final String[] options;

    public OptionSelector(String[] options) {
        this.options = options;
    }

    public final int select(InputStream stream, boolean b) {
        int index;

        while (true) {
            Scanner scanner = new Scanner(stream);

            if (b) {
                System.out.println("-------- Options --------");

                for (int i = 0; i < this.options.length; i++) {
                    System.out.println((i + 1) + ". " + this.options[i]);
                }

                System.out.println("-------------------------");
                System.out.print(" > ");
            }

            try {
                index = scanner.nextInt();

                if (index <= 0 || index > this.options.length) {
                    if (b) {
                        System.out.println("正しい値を入力してください。");
                    }

                    continue;
                }
            } catch (InputMismatchException e) {
                if (b) {
                    System.out.println("正しい値を入力してください。");
                }

                continue;
            }

            break;
        }

        return index - 1;
    }
}
