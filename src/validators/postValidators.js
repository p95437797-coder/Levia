'use strict';
const { z } = require('zod');
const postIdParams = z.object({ postId: z.string().uuid() });
const createPost = z.object({
  body: z.string().trim().min(1).max(2000),
  category: z.enum(['thought','poetry','shayari','daily_thought','quote','love','sadness','motivation','friendship','life','attitude','funny','other']).default('thought'),
  commentsEnabled: z.boolean().optional().default(true)
});
const commentBody = z.object({ body: z.string().trim().min(1).max(1000) });
const pageQuery = z.object({ limit: z.coerce.number().int().min(1).max(30).default(20), cursor: z.string().datetime().optional() });
const searchQuery = z.object({ q: z.string().trim().min(1).max(100), limit: z.coerce.number().int().min(1).max(30).default(20) });
const usernameParams = z.object({ username: z.string().regex(/^[a-z0-9_.]{3,30}$/i).transform(v => v.toLowerCase()) });
module.exports = { postIdParams, createPost, commentBody, pageQuery, searchQuery, usernameParams };
