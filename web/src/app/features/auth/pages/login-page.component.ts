import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/services/auth.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <main class="login-shell">
      <section class="brand-panel" aria-label="AutoFlow">
        <div class="brand"><span class="brand-mark">A</span><span>autoflow</span></div>
        <div class="brand-copy">
          <span class="eyebrow">GESTÃO DE OFICINA</span>
          <h1>Sua oficina,<br>em movimento.</h1>
          <p>Organize o dia a dia da sua operação em um só lugar.</p>
        </div>
        <div class="panel-foot"><span class="status-dot"></span> Tudo pronto para começar</div>
        <div class="orb orb-one"></div><div class="orb orb-two"></div>
      </section>

      <section class="form-panel">
        <div class="form-wrap">
          <div class="mobile-brand"><span class="brand-mark">A</span> autoflow</div>
          <span class="eyebrow">BEM-VINDO DE VOLTA</span>
          <h2>Acesse sua conta</h2>
          <p class="form-intro">Entre com seus dados para continuar.</p>

          <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
            <label for="email">E-mail</label>
            <input id="email" type="email" formControlName="email" autocomplete="username" placeholder="voce@oficina.com.br">
            @if (form.controls.email.touched && form.controls.email.invalid) {
              <span class="field-error">Informe um e-mail válido.</span>
            }

            <div class="password-label"><label for="password">Senha</label></div>
            <input id="password" type="password" formControlName="password" autocomplete="current-password" placeholder="Sua senha">
            @if (form.controls.password.touched && form.controls.password.invalid) {
              <span class="field-error">A senha deve ter pelo menos 8 caracteres.</span>
            }

            @if (error()) { <div class="error-banner" role="alert">{{ error() }}</div> }
            @if (success()) { <div class="success-banner" role="status">{{ success() }}</div> }
            <button type="submit" [disabled]="form.invalid || loading()">
              @if (loading()) { <span class="spinner"></span> Entrando... } @else { Entrar <span aria-hidden="true">→</span> }
            </button>
          </form>
          <p class="form-note">Problemas para acessar? Entre em contato com o administrador da sua oficina.</p>
          <div class="copyright">© 2026 AutoFlow · Gestão que acompanha seu ritmo</div>
        </div>
      </section>
    </main>
  `
})
export class LoginPageComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly success = signal('');
  readonly form = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  submit(): void {
    this.error.set('');
    this.success.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.loading.set(false);
        void this.router.navigate(['/dashboard']);
      },
      error: (response: HttpErrorResponse) => {
        this.loading.set(false);
        this.error.set(response.status === 0
          ? 'Não foi possível conectar à API. Verifique se os serviços estão em execução.'
          : response.status === 401
            ? 'E-mail ou senha inválidos.'
            : 'Não foi possível entrar agora. Tente novamente.');
      }
    });
  }
}
