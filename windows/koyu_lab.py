import csv
import hashlib
import math
import secrets
import tkinter as tk
from tkinter import ttk, filedialog, messagebox
from datetime import datetime

APP = "KOYU Mnemonic Lab 1.0"

def collision_probability(bits: int, n: int) -> float:
    if n <= 1:
        return 0.0
    space = 2 ** bits
    return -math.expm1(-(n * (n - 1)) / (2.0 * space))

def run_collision(bits: int, trials: int):
    seen = set()
    for i in range(1, trials + 1):
        value = secrets.randbits(bits)
        if value in seen:
            return i, value
        seen.add(value)
    return None, None

def simulated_ids():
    out = []
    for chain, prefix in [
        ("BTC", "BTC-TEST-SIM"),
        ("ETH", "ETH-TEST-SIM"),
        ("BNB", "BNB-TEST-SIM"),
        ("SOL", "SOL-TEST-SIM"),
    ]:
        raw = secrets.token_bytes(32)
        digest = hashlib.sha256(raw).hexdigest()
        out.append((chain, f"{prefix}-{digest[:32]}"))
    return out

class App:
    def __init__(self, root):
        self.root = root
        self.root.title(APP)
        self.root.geometry("760x650")
        self.root.minsize(650, 560)
        self.root.configure(bg="#10141d")
        self.records = []
        self._build()

    def label(self, parent, text, size=11, color="#e8edf5", bold=False):
        return tk.Label(parent, text=text, bg="#10141d", fg=color,
                        font=("Segoe UI", size, "bold" if bold else "normal"))

    def _build(self):
        self.label(self.root, "KOYU MNEMONIC LAB", 21, "#70d6ff", True).pack(anchor="w", padx=24, pady=(20, 2))
        self.label(self.root, "数学碰撞模拟  /  多链测试标识符实验", 10, "#9aa8ba").pack(anchor="w", padx=25, pady=(0, 16))

        panel = tk.Frame(self.root, bg="#1a2230", padx=16, pady=14)
        panel.pack(fill="x", padx=22, pady=5)
        tk.Label(panel, text="碰撞空间位数（4–32）", bg="#1a2230", fg="#e8edf5").grid(row=0, column=0, sticky="w")
        self.bits = tk.StringVar(value="16")
        ttk.Entry(panel, textvariable=self.bits, width=12).grid(row=0, column=1, padx=12, sticky="w")
        tk.Label(panel, text="最大抽样次数", bg="#1a2230", fg="#e8edf5").grid(row=1, column=0, sticky="w", pady=(12,0))
        self.trials = tk.StringVar(value="100000")
        ttk.Entry(panel, textvariable=self.trials, width=12).grid(row=1, column=1, padx=12, sticky="w", pady=(12,0))
        ttk.Button(panel, text="运行碰撞实验", command=self.run).grid(row=0, column=2, rowspan=2, padx=12)

        self.result = tk.Text(self.root, height=11, bg="#0b0f16", fg="#dce7f5",
                              insertbackground="white", relief="flat", wrap="word",
                              font=("Consolas", 10), padx=14, pady=12)
        self.result.pack(fill="both", expand=True, padx=22, pady=12)
        self.result.insert("end", "等待实验……\n\n提示：降低位数更容易观察到碰撞；增加位数会显著降低碰撞概率。\n")

        bottom = tk.Frame(self.root, bg="#10141d")
        bottom.pack(fill="x", padx=22, pady=(0, 8))
        ttk.Button(bottom, text="生成多链模拟标识符", command=self.make_ids).pack(side="left")
        ttk.Button(bottom, text="导出 CSV", command=self.export).pack(side="left", padx=10)

        warning = ("安全说明：这里生成的是模拟标识符，不是真实钱包地址；"
                   "不会扫描主网、查询陌生人余额或保存助记词/私钥。")
        self.label(self.root, warning, 9, "#ffca7a").pack(anchor="w", padx=24, pady=(0, 16))

    def write(self, text):
        self.result.insert("end", text + "\n")
        self.result.see("end")

    def run(self):
        try:
            bits = int(self.bits.get())
            trials = int(self.trials.get())
            if not 4 <= bits <= 32:
                raise ValueError("位数必须在 4 到 32 之间。")
            if not 1 <= trials <= 2_000_000:
                raise ValueError("抽样次数必须在 1 到 2,000,000 之间。")
        except ValueError as e:
            messagebox.showerror("参数错误", str(e))
            return
        self.write(f"\n[{datetime.now().isoformat(timespec='seconds')}] 开始实验：{bits} bit，最多 {trials:,} 次")
        self.root.update_idletasks()
        at, value = run_collision(bits, trials)
        p = collision_probability(bits, trials)
        if at is None:
            self.write(f"结果：未在 {trials:,} 次内发现重复值。")
        else:
            self.write(f"结果：第 {at:,} 次抽样发现首次重复。重复值（仅实验编号）：{value}")
        self.write(f"理论至少一次碰撞概率（生日悖论近似）：{p*100:.6f}%")
        self.records.append({
            "time": datetime.now().isoformat(timespec="seconds"),
            "type": "collision",
            "bits": bits,
            "trials": trials,
            "first_collision_at": at if at is not None else "",
            "theoretical_probability": f"{p:.12f}",
            "details": str(value) if value is not None else "no collision"
        })

    def make_ids(self):
        self.write("\n多链模拟标识符（不可用于链上交易）：")
        for chain, identifier in simulated_ids():
            self.write(f"{chain}: {identifier}")
            self.records.append({
                "time": datetime.now().isoformat(timespec="seconds"),
                "type": "simulated_identifier",
                "bits": "",
                "trials": "",
                "first_collision_at": "",
                "theoretical_probability": "",
                "details": f"{chain}:{identifier}"
            })

    def export(self):
        if not self.records:
            messagebox.showinfo("暂无数据", "请先运行实验或生成模拟标识符。")
            return
        path = filedialog.asksaveasfilename(
            defaultextension=".csv", filetypes=[("CSV 文件", "*.csv")],
            initialfile="koyu_mnemonic_lab_results.csv")
        if not path:
            return
        with open(path, "w", newline="", encoding="utf-8-sig") as f:
            writer = csv.DictWriter(f, fieldnames=list(self.records[0].keys()))
            writer.writeheader()
            writer.writerows(self.records)
        messagebox.showinfo("导出完成", f"已保存：{path}")

if __name__ == "__main__":
    root = tk.Tk()
    App(root)
    root.mainloop()
