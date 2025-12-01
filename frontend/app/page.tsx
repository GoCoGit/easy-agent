"use client";

import {
  useCallback,
  useEffect,
  useRef,
  useState,
  type KeyboardEvent,
  type ChangeEvent,
} from "react";

import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";

import { message } from "antd";
import { request, baseURL } from "../lib/request";

type Message = {
  id: number;
  sender: "me" | "them";
  text: string;
  done?: boolean;
};

const USER_ID = "123";

export default function Home() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState("");
  const [isSending, setIsSending] = useState(false);
  const [searchMode, setSearchMode] = useState<"common" | "rag" | "web">("common");
  const listRef = useRef<HTMLDivElement>(null);
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const [messageApi, contextHolder] = message.useMessage();

  // 追加助手消息
  const appendAssistantMessage = useCallback(
    (chunk: string) => {
      if (!chunk) return;
      setMessages((prev) => {

        if (prev.length) {
          // 最新的一条消息
          const last = prev[prev.length - 1];

          // 如果消息发送者是对方
          if (last.sender === "them") {
            const updated = [...prev];
            updated[prev.length - 1] = {
              ...last,
              text: `${last.text}${chunk}`,
            };
            return updated;
          }
        }

        const newId = Date.now() + Math.random();
        return [
          ...prev,
          {
            id: newId,
            sender: "them",
            text: chunk,
          },
        ];
      });
    },
    []
  );

  useEffect(() => {
    const source = new EventSource(
      `${baseURL}/sse/connect?userId=${USER_ID}`
    );

    source.onmessage = (event) => {
      if (!event.data) return;

      const chunk = event.data;

      appendAssistantMessage(chunk);
    };

    source.onerror = () => {
      source.close();
    };

    return () => {
      source.close();
    };
  }, [appendAssistantMessage]);

  // 滚动到底部
  useEffect(() => {
    if (listRef.current) {
      listRef.current.scrollTop = listRef.current.scrollHeight;
    }
  }, [messages]);

  // 发送消息
  const sendMessage = async () => {
    const text = input.trim();
    if (!text) return;
    setInput("");
    setMessages((prev) => [
      ...prev,
      { id: Date.now(), sender: "me", text, done: true },
    ]);
    try {
      setIsSending(true);
      const endpoint =
        searchMode === "rag"
          ? "/chat/rag"
          : searchMode === "web"
            ? "/chat/web"
            : "/chat/common";

      await request.get(endpoint, {
        params: {
          msg: text,
        },
      });
    } catch (error) {
      console.error("Failed to send message", error);
    } finally {
      setIsSending(false);
    }
  };

  const handleUploadClick = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = async (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("file", file);

    try {
      const res = await request.post(`/rag/upload`, formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });

      const data = res.data;

      if (!data || data.status !== 200) {
        console.error("Upload failed", data);
        messageApi.open({
          type: "error",
          content: "上传失败，请稍后重试",
        });
      } else {
        messageApi.open({
          type: "success",
          content: "上传成功",
        });
      }
    } catch (error) {
      console.error("Failed to upload document", error);
      messageApi.open({
        type: "error",
        content: "上传失败，请检查网络后重试",
      });
    } finally {
      // 允许再次选择同一个文件
      event.target.value = "";
    }
  };

  const handleModeToggle = (mode: "rag" | "web") => {
    setSearchMode((prev) => (prev === mode ? "common" : mode));
  };

  const getModeClasses = (mode: "rag" | "web") => {
    const isActive = searchMode === mode;
    return `px-4 py-2 rounded border-2 font-semibold cursor-pointer transition-colors ${isActive
      ? "border-blue-600 text-white bg-blue-600 shadow"
      : "border-blue-500 text-blue-500 hover:bg-blue-50 hover:text-blue-600"
      }`;
  };

  const handleKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
    // isComposing表示键盘事件是否发生在输入法组合阶段
    // 兼容处理：没有isComposing属性的浏览器，处于组合阶段的事件keyCode统一是229
    const isComposing =
      Boolean(event.nativeEvent.isComposing) || event.nativeEvent.keyCode === 229;

    // 按了Enter，且不处于组合阶段，执行发送
    if (event.key === "Enter" && !event.shiftKey && !isComposing) {
      event.preventDefault();
      sendMessage();
    }
  };

  return (
    <main
      style={{
        minHeight: "100vh",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        background: "linear-gradient(135deg, #cfe2ff, #f8d7ff)",
      }}
    >
      {contextHolder}
      <div
        className="overflow-hidden rounded-2xl bg-white shadow-2xl flex flex-col justify-center items-center w-[1200px] h-[800px]"
      >
        <div className="w-full h-[8%] flex justify-center items-center bg-blue-500 text-white font-bold text-2xl">
          Easy Agent
        </div>
        <div
          ref={listRef}
          className="w-full h-[74%] flex flex-col gap-4 overflow-y-auto bg-white/70 p-8"
        >
          {messages.map((message) => {
            const isMine = message.sender === "me";
            return (
              <div
                key={message.id}
                className={`flex items-start gap-3 ${isMine ? "justify-end" : "justify-start"
                  }`}
              >
                {!isMine && (
                  <div className="flex h-11 w-11 items-center justify-center rounded-full bg-slate-200 text-sm font-semibold text-slate-600">
                    AI
                  </div>
                )}
                <div
                  className={`max-w-[65%] rounded-2xl px-4 py-2 text-lg text-slate-800 shadow ${isMine ? "bg-emerald-400 text-white" : "bg-slate-100"
                    }`}
                >
                  <ReactMarkdown remarkPlugins={[remarkGfm]}>
                    {message.text}
                  </ReactMarkdown>
                </div>
                {isMine && (
                  <div className="flex h-11 w-11 items-center justify-center rounded-full bg-emerald-500 text-sm font-semibold text-white">
                    我
                  </div>
                )}
              </div>
            );
          })}
        </div>
        <div className="w-full h-[6%] flex items-center px-6 py-2 gap-3">
          <div
            className={getModeClasses("web")}
            onClick={() => handleModeToggle("web")}
          >
            联网搜索
          </div>
          <div
            className={getModeClasses("rag")}
            onClick={() => handleModeToggle("rag")}
          >
            知识库搜索
          </div>
          <div
            className="px-4 py-2 rounded border-2 border-blue-500 text-blue-500 font-semibold cursor-pointer transition-colors hover:bg-blue-50 hover:text-blue-600"
            onClick={handleUploadClick}
          >
            上传文档
          </div>
          <input
            ref={fileInputRef}
            type="file"
            className="hidden"
            onChange={handleFileChange}
          />
        </div>
        <div className="w-full h-[12%] flex justify-center items-center p-6 pt-3">
          <div className="flex w-full h-full gap-4">
            <input
              type="text"
              placeholder="请输入内容"
              value={input}
              onChange={(event) => setInput(event.target.value)}
              onKeyDown={handleKeyDown}
              className="flex-1 h-full rounded-xl border border-slate-200 bg-white/80 px-4 text-lg text-slate-700 outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
            />
            <button
              onClick={sendMessage}
              disabled={isSending}
              className="h-full rounded-xl bg-blue-500 px-8 text-lg font-semibold text-white shadow-md transition hover:bg-blue-600 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-500 disabled:cursor-not-allowed disabled:bg-blue-400"
            >
              发送
            </button>
          </div>
        </div>
      </div>
    </main>
  );
}