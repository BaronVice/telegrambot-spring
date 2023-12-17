package com.jeduler.telegramalpha.impl;

import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.Map;

import static com.jeduler.telegramalpha.impl.Constants.START_TEXT;
import static com.jeduler.telegramalpha.impl.UserState.*;

public class ResponseHandler {
    private final SilentSender sender;
    private final Map<Long, UserState> chatStates;

    public ResponseHandler(SilentSender sender, DBContext db) {
        this.sender = sender;
        chatStates = db.getMap(Constants.CHAT_STATES);
    }

    public void replyToStart(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(START_TEXT);
        sender.execute(message);
        chatStates.put(chatId, AWAITING_NAME);
    }

    public void replyToButtons(long chatId, Message message) {
        if (message.getText().equalsIgnoreCase("/stop")) {
            stopChat(chatId);
        }

        switch (chatStates.get(chatId)) {
            case AWAITING_NAME -> replyToName(chatId, message);
            case FOOD_DRINK_SELECTION -> replyToFoodDrinkSelection(chatId, message);
            case PIZZA_TOPPINGS -> replyToPizzaToppings(chatId, message);
            case AWAITING_CONFIRMATION -> replyToOrder(chatId, message);
            default -> unexpectedMessage(chatId);
        }
    }

    private void unexpectedMessage(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Пожалуйста, введите существующую комманду.");
        sender.execute(sendMessage);
    }

    private void stopChat(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Выздоравливайте!\nВведите /start чтобы заказать снова");
        chatStates.remove(chatId);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sender.execute(sendMessage);
    }

    private void replyToOrder(long chatId, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        if ("да".equalsIgnoreCase(message.getText())) {
            sendMessage.setText("Заказ оформлен и скоро будет доставлен!\nЗаказать еще?");
            sendMessage.setReplyMarkup(KeyboardFactory.getPizzaOrDrinkKeyboard());
            sender.execute(sendMessage);
            chatStates.put(chatId, FOOD_DRINK_SELECTION);
        } else if ("нет".equalsIgnoreCase(message.getText())) {
            stopChat(chatId);
        } else {
            sendMessage.setText("Пожалуйста, введите да или нет");
            sendMessage.setReplyMarkup(KeyboardFactory.getYesOrNo());
            sender.execute(sendMessage);
        }
    }

    private void replyToPizzaToppings(long chatId, Message message) {
        if ("Странный грипп".equalsIgnoreCase(message.getText())) {
            promptWithKeyboardForState(chatId, "Странный грипп?\nМы положили в корзину странный сироп!\nЗаказать снова?",
                    KeyboardFactory.getYesOrNo(), AWAITING_CONFIRMATION);
        } else if ("Грипп необыкновенный".equalsIgnoreCase(message.getText())) {
            promptWithKeyboardForState(chatId, "Необыкновенно вкусный сироп против необыкновенного гриппа!\nЧто-нибудь еще?",
                    KeyboardFactory.getPizzaToppingsKeyboard(), PIZZA_TOPPINGS);
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText("Мы не знаем вид гриппа \"" + message.getText() + "\".\nВыберите доступный вид гриппа!");
            sendMessage.setReplyMarkup(KeyboardFactory.getPizzaToppingsKeyboard());
            sender.execute(sendMessage);
        }
    }

    private void promptWithKeyboardForState(long chatId, String text, ReplyKeyboard YesOrNo, UserState awaitingReorder) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(YesOrNo);
        sender.execute(sendMessage);
        chatStates.put(chatId, awaitingReorder);
    }

    private void replyToFoodDrinkSelection(long chatId, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        if ("Таблетки".equalsIgnoreCase(message.getText())) {
            sendMessage.setText("Таблетки еще не изобрели (((");
            sendMessage.setReplyMarkup(KeyboardFactory.getPizzaOrDrinkKeyboard());
            sender.execute(sendMessage);
        } else if ("Сироп".equalsIgnoreCase(message.getText())) {
            sendMessage.setText("Конечно!\nОт какого гриппа вам сироп?");
            sendMessage.setReplyMarkup(KeyboardFactory.getPizzaToppingsKeyboard());
            sender.execute(sendMessage);
            chatStates.put(chatId, UserState.PIZZA_TOPPINGS);
        } else {
            sendMessage.setText("Мы такое не продаем. Выберите из того, что у нас есть.");
            sendMessage.setReplyMarkup(KeyboardFactory.getPizzaOrDrinkKeyboard());
            sender.execute(sendMessage);
        }
    }

    private void replyToName(long chatId, Message message) {
        promptWithKeyboardForState(chatId, "Приветсвую " + message.getText() + ". Что будете брать в этот раз?",
                KeyboardFactory.getPizzaOrDrinkKeyboard(),
                FOOD_DRINK_SELECTION);
    }

    public boolean userIsActive(Long chatId) {
        return chatStates.containsKey(chatId);
    }
}
