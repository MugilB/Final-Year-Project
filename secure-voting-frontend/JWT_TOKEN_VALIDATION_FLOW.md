# JWT Token Validation Flow

## Summary: **YES, each API call checks the JWT token on the backend**

## How It Works

### 🔵 **Frontend Side (Token Sending)**

#### Current Implementation:
Currently, **each service manually adds the token** to requests using `getAuthHeaders()`:

```typescript
// In AuthService
public getAuthHeaders(): HttpHeaders {
  const token = this.getToken(); // Gets token from cookie
  return new HttpHeaders({
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  });
}

// In DataService - Each API call explicitly includes headers
getAdminUsers(): Observable<any> {
  return this.http.get(API_URL + '/admin/users', { 
    headers: this.getAuthHeaders() // ✅ Token added manually
  });
}
```

#### Note:
- ❌ **No HTTP Interceptor** - Tokens are added manually to each request
- Some endpoints are public (don't need token): `/api/elections`, `/api/elections/open`
- Protected endpoints require token: `/api/users/me`, `/api/vote`, `/api/admin/**`

---

### 🟢 **Backend Side (Token Validation)**

#### 1. **AuthTokenFilter** - Runs on EVERY Request

Located: `AuthTokenFilter.java`

```java
@Override
protected void doFilterInternal(HttpServletRequest request, 
                                HttpServletResponse response, 
                                FilterChain filterChain) {
    try {
        // 1. Extract JWT from Authorization header
        String jwt = parseJwt(request);
        
        // 2. Validate token
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            // 3. Extract username from token
            String username = jwtUtils.getUserNameFromJwtToken(jwt);
            
            // 4. Load user details from database
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // 5. Set authentication in Spring Security context
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, 
                    userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    } catch (Exception e) {
        logger.error("Cannot set user authentication: {}", e);
    }
    
    // Continue with request processing
    filterChain.doFilter(request, response);
}

// Extract token from "Authorization: Bearer <token>" header
private String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");
    if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
        return headerAuth.substring(7, headerAuth.length());
    }
    return null;
}
```

#### 2. **JwtUtils** - Validates Token

Located: `JwtUtils.java`

```java
public boolean validateJwtToken(String authToken) {
    try {
        // Validates:
        // - Token signature (using secret key)
        // - Token expiration date
        // - Token format
        Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(authToken);
        return true;
    } catch (SecurityException e) {
        logger.error("Invalid JWT signature: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
        logger.error("JWT token is expired: {}", e.getMessage());
    } catch (MalformedJwtException e) {
        logger.error("Invalid JWT token: {}", e.getMessage());
    }
    // ... other exceptions
    return false;
}
```

#### 3. **WebSecurityConfig** - Configures Protected Routes

Located: `WebSecurityConfig.java`

```java
@Override
protected void configure(HttpSecurity http) {
    // Public endpoints (no token required)
    .authorizeRequests()
        .antMatchers("/api/auth/**").permitAll()
        .antMatchers("/api/elections").permitAll()
        .antMatchers("/api/elections/open").permitAll()
        // ... more public endpoints
        
    // Protected endpoints (token required)
        .antMatchers("/api/users/me").authenticated()
        .antMatchers("/api/voters/all").hasRole("ADMIN")
        .antMatchers("/api/admin/**").hasRole("ADMIN")
        .anyRequest().authenticated(); // All other requests need token
    
    // Register the filter that checks JWT on EVERY request
    http.addFilterBefore(
        authenticationJwtTokenFilter(), 
        UsernamePasswordAuthenticationFilter.class
    );
}
```

---

## 🔄 **Complete Request Flow**

```
1. Frontend makes API call
   └─> HTTP Request with Authorization header
       └─> Authorization: Bearer <JWT_TOKEN>

2. Request arrives at Spring Boot Backend
   └─> AuthTokenFilter intercepts request (FIRST)

3. AuthTokenFilter checks:
   ├─> Extracts token from "Authorization: Bearer <token>" header
   ├─> Calls jwtUtils.validateJwtToken(token)
   │   ├─> Validates signature (secret key match)
   │   ├─> Checks expiration date
   │   └─> Verifies token format
   │
   ├─> If valid:
   │   ├─> Extracts username (voterId) from token
   │   ├─> Loads user details from database
   │   └─> Sets authentication in Spring Security context
   │
   └─> Continues to controller

4. Controller receives request
   └─> Can check authentication status:
       ├─> @PreAuthorize("hasRole('ADMIN')") - Role-based access
       ├─> SecurityContextHolder.getContext().getAuthentication()
       └─> Returns response or throws 401/403 if unauthorized

5. Response sent back to frontend
```

---

## 📊 **Request Examples**

### ✅ **Protected Request (With Token)**
```http
GET /api/users/me HTTP/1.1
Host: localhost:8081
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJWT1RFUl8xMjM0...
```

**Backend Process:**
1. AuthTokenFilter extracts token
2. Validates token ✅
3. Loads user details ✅
4. Sets authentication ✅
5. Controller processes request ✅
6. Returns user data ✅

### ❌ **Protected Request (Without Token)**
```http
GET /api/users/me HTTP/1.1
Host: localhost:8081
(No Authorization header)
```

**Backend Process:**
1. AuthTokenFilter finds no token
2. No authentication set
3. WebSecurityConfig checks: `.antMatchers("/api/users/me").authenticated()`
4. Returns **401 Unauthorized** ❌

### ✅ **Public Request (No Token Needed)**
```http
GET /api/elections/open HTTP/1.1
Host: localhost:8081
(No Authorization header)
```

**Backend Process:**
1. AuthTokenFilter runs but finds no token (OK for public endpoints)
2. WebSecurityConfig: `.antMatchers("/api/elections/open").permitAll()` ✅
3. Request proceeds without authentication ✅

---

## 🎯 **Key Points**

1. **Every request goes through AuthTokenFilter** - It's registered in the filter chain
2. **Token validation happens BEFORE controller** - Filter runs first
3. **Invalid/expired tokens are rejected** - Returns 401 Unauthorized
4. **Public endpoints bypass authentication** - Explicitly configured in WebSecurityConfig
5. **Token stored in cookies** - Frontend stores in cookies (recently changed from sessionStorage)

---

## 🔧 **Potential Improvement: Frontend HTTP Interceptor**

Currently, tokens are added **manually** to each request. Consider creating an HTTP Interceptor to **automatically add the token**:

```typescript
// auth.interceptor.ts
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    
    if (token && !this.isPublicEndpoint(req.url)) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }
    
    return next.handle(req);
  }
  
  private isPublicEndpoint(url: string): boolean {
    // Check if endpoint is public
    return url.includes('/api/auth/') || 
           url.includes('/api/elections/open');
  }
}
```

This would eliminate the need to manually call `getAuthHeaders()` on every request.









