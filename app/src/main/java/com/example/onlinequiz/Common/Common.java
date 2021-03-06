package com.example.onlinequiz.Common;

import com.example.onlinequiz.Model.Question;
import com.example.onlinequiz.Model.Test;
import com.example.onlinequiz.Model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Common {
    public static String categoryId, categoryName;
    public static User currentUser;
    public static int testQuestionQty = 5;
    public static List<Question> questionsList = new ArrayList<>();
    public static List<Question> questionsRight = new ArrayList<>();
    public static List<Question> questionsWrong = new ArrayList<>();

    // FOR TEST DETAIL
    public static Test test;

    public static void shuffleQuestionList() {
        Collections.shuffle(Common.questionsList);
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        Common.currentUser = currentUser;
    }

    public static Test getTest() {
        return test;
    }

    public static void setTest(Test test) {
        Common.test = test;
    }

}
