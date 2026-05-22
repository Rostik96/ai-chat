package dev.rost.aichat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
class ChatResource {

    private final ChatService service;

    @GetMapping
    ModelAndView index(ModelAndView view) {
        view.setViewName("chat");
        view.addObject("chats", service.getAll());
        return view;
    }

    @PostMapping("/new")
    RedirectView create(@RequestParam String title) {
        var chat = service.create(title);
        return new RedirectView("/chat/" + chat.id());
    }

    @GetMapping("/{id:\\d+}")
    ModelAndView read(@PathVariable Long id, ModelAndView view) {
        view.setViewName("chat");
        view.addObject("chats", service.getAll());
        view.addObject("chat", service.get(id));
        return view;
    }

    @DeleteMapping("/{id:\\d+}")
    RedirectView delete(@PathVariable Long id) {
        service.delete(id);
        return new RedirectView("/chat");
    }
}
