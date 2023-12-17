package com.jeduler.telegramalpha.impl;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

public class KeyboardFactory {
    public static ReplyKeyboard getPizzaToppingsKeyboard() {
        KeyboardRow row = new KeyboardRow();
        row.add("Странный грипп");
        row.add("Грипп необыкновенный");
        return new ReplyKeyboardMarkup(List.of(row));
    }

    public static ReplyKeyboard getPizzaOrDrinkKeyboard(){
        KeyboardRow row = new KeyboardRow();
        row.add("Таблетки");
        row.add("Сироп");
        return new ReplyKeyboardMarkup(List.of(row));
    }

    public static ReplyKeyboard getYesOrNo() {
        KeyboardRow row = new KeyboardRow();
        row.add("Да");
        row.add("Нет");
        return new ReplyKeyboardMarkup(List.of(row));
    }
}