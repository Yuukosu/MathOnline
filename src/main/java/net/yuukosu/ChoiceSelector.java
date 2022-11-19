package net.yuukosu;

import lombok.Getter;

import java.io.InputStream;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ChoiceSelector {

    @Getter
    private final String[] choices;

    public ChoiceSelector(String[] choices) {
        this.choices = choices;
    }

    public final int select(InputStream stream, boolean b) {
        int index;

        while (true) {
            Scanner scanner = new Scanner(stream);

            if (b) {
                System.out.println("-------- Choices --------");

                for (int i = 0; i < this.choices.length; i++) {
                    System.out.println((i + 1) + ". " + this.choices[i]);
                }

                System.out.println("-------------------------");
                System.out.print(" > ");
            }

            try {
                index = scanner.nextInt();

                if (index <= 0 || index > choices.length) {
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

    public String getChoice(int index) {
        return this.choices[index];
    }
}
